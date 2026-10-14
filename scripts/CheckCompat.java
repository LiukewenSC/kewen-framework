import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * 用 H2 的 MySQL 兼容模式逐条执行 SQL 脚本，检查语法兼容、可重复执行、以及对象是否真的建成。
 * 不用 H2 自带的 RUNSCRIPT：它会吞掉脚本内部的错误。
 * 用法：java -cp h2.jar CheckCompat.java [--quiet] file1.sql file2.sql ...
 *
 * @author kewen
 */
public class CheckCompat {

    public static void main(String[] args) throws Exception {
        List<String> files = new ArrayList<>();
        boolean quiet = false;
        for (String a : args) {
            if ("--quiet".equals(a)) quiet = true;
            else files.add(a);
        }
        if (files.isEmpty()) {
            System.out.println("用法：java -cp h2.jar CheckCompat.java file1.sql file2.sql ...");
            System.exit(2);
        }

        String url = "jdbc:h2:mem:check;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
        // 源文件模式下 ServiceLoader 不一定能发现 h2 的驱动，显式注册
        Class.forName("org.h2.Driver");
        int expectedTables = 0;
        for (String f : files) expectedTables += countCreateTables(f);

        int problems = 0;
        for (int pass = 1; pass <= 2; pass++) {
            List<String> failures = new ArrayList<>();
            try (Connection c = DriverManager.getConnection(url, "sa", "")) {
                for (String f : files) {
                    for (String st : split(read(f))) {
                        try (Statement s = c.createStatement()) {
                            s.execute(st);
                        } catch (SQLException e) {
                            failures.add("  " + brief(f) + " | " + firstLine(st) + "\n      <<< " + e.getMessage().split("\n")[0]);
                        }
                    }
                }
                int actual = tableCount(c);
                if (actual < expectedTables) {
                    failures.add("  只建出 " + actual + " 张表，脚本里共有 " + expectedTables + " 条 CREATE TABLE");
                }
                if (!quiet) System.out.println("第 " + pass + " 遍：" + actual + " 张表（应 " + expectedTables + "）");
            }
            if (failures.isEmpty()) {
                System.out.println("ok  第 " + pass + " 遍通过");
            } else {
                System.out.println("!! 第 " + pass + " 遍失败：");
                for (String s : failures) System.out.println(s);
                problems++;
            }
        }
        System.exit(problems == 0 ? 0 : 1);
    }

    static String read(String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), "UTF-8");
    }

    static String brief(String f) {
        String n = f.replace('\\', '/');
        int i = n.lastIndexOf('/');
        return i < 0 ? n : n.substring(i + 1);
    }

    static String firstLine(String st) {
        for (String l : st.split("\n")) {
            String t = l.trim();
            if (!t.isEmpty() && !t.startsWith("--") && !t.startsWith("/*")) return t;
        }
        return st.trim();
    }

    /** 只统计真正语句位置的 CREATE TABLE，跳过注释里的 */
    static int countCreateTables(String file) throws Exception {
        int n = 0;
        for (String st : split(read(file))) {
            if (firstLine(st).toUpperCase().startsWith("CREATE TABLE")) n++;
        }
        return n;
    }

    static int tableCount(Connection c) throws Exception {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_TYPE='BASE TABLE'")) {
            return r.next() ? r.getInt(1) : -1;
        }
    }

    /** 按分号切语句，识别单引号、双引号、反引号、行注释和块注释，避免把字符串里的分号当分隔符 */
    static List<String> split(String src) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        char quote = 0;
        int i = 0;
        while (i < src.length()) {
            char ch = src.charAt(i);
            if (quote != 0) {
                cur.append(ch);
                if (ch == '\\' && quote != '`' && i + 1 < src.length()) {
                    cur.append(src.charAt(++i));
                } else if (ch == quote) {
                    quote = 0;
                }
                i++;
                continue;
            }
            if (ch == '\'' || ch == '"' || ch == '`') {
                quote = ch;
                cur.append(ch);
                i++;
                continue;
            }
            if (ch == '-' && i + 1 < src.length() && src.charAt(i + 1) == '-') {
                while (i < src.length() && src.charAt(i) != '\n') cur.append(src.charAt(i++));
                continue;
            }
            if (ch == '/' && i + 1 < src.length() && src.charAt(i + 1) == '*') {
                cur.append("/*");
                i += 2;
                while (i + 1 < src.length() && !(src.charAt(i) == '*' && src.charAt(i + 1) == '/')) cur.append(src.charAt(i++));
                cur.append("*/");
                i += 2;
                continue;
            }
            if (ch == ';') {
                if (!cur.toString().trim().isEmpty()) out.add(cur.toString());
                cur.setLength(0);
                i++;
                continue;
            }
            cur.append(ch);
            i++;
        }
        if (!cur.toString().trim().isEmpty()) out.add(cur.toString());
        return out;
    }
}
