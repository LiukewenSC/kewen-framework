import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验 docs/application.yml 是否覆盖 spring-configuration-metadata.json 中的全部配置键。
 * 只读取 metadata 的 properties 数组（groups 是前缀，不算键）；List 元素字段不在 metadata 中，不参与校验。
 * 用法：java -cp snakeyaml.jar CheckConfigDocs.java <application.yml> <metadata.json...>
 *
 * @author kewen
 */
public class CheckConfigDocs {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("用法：java -cp snakeyaml.jar CheckConfigDocs.java <application.yml> <metadata.json...>");
            System.exit(2);
        }
        Path doc = Paths.get(args[0]).toAbsolutePath().normalize();
        Set<String> yamlPaths = flattenYaml(doc);

        Set<String> metadataNames = new TreeSet<>();
        for (int i = 1; i < args.length; i++) {
            metadataNames.addAll(readPropertyNames(Paths.get(args[i])));
        }

        List<String> missing = new ArrayList<>();
        for (String name : metadataNames) {
            if (!yamlPaths.contains(name)) {
                missing.add(name);
            }
        }

        System.out.println("YAML 路径节点：" + yamlPaths.size());
        System.out.println("metadata 配置键：" + metadataNames.size() + "（来自 " + (args.length - 1) + " 个文件）");
        if (!missing.isEmpty()) {
            System.out.println("缺失 " + missing.size() + " 个键：");
            for (String name : missing) {
                System.out.println("  - " + name);
            }
            System.exit(1);
        }
        System.out.println("全部配置键均已出现在 " + doc);
    }

    private static Set<String> flattenYaml(Path doc) throws Exception {
        if (!Files.isRegularFile(doc)) {
            throw new IllegalStateException("找不到 YAML 文件：" + doc);
        }
        Object root;
        try (Reader reader = Files.newBufferedReader(doc, StandardCharsets.UTF_8)) {
            root = new Yaml().load(reader);
        }
        if (!(root instanceof Map)) {
            throw new IllegalStateException("YAML 根节点不是映射：" + doc);
        }
        Set<String> paths = new TreeSet<>();
        flatten("", (Map<?, ?>) root, paths);
        return paths;
    }

    private static void flatten(String prefix, Map<?, ?> map, Set<String> paths) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String path = prefix.isEmpty() ? String.valueOf(entry.getKey()) : prefix + "." + entry.getKey();
            paths.add(path);
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten(path, (Map<?, ?>) value, paths);
            }
        }
    }

    private static Set<String> readPropertyNames(Path json) throws Exception {
        if (!Files.isRegularFile(json)) {
            throw new IllegalStateException("找不到 metadata 文件：" + json);
        }
        String text = new String(Files.readAllBytes(json), StandardCharsets.UTF_8);
        int properties = text.indexOf("\"properties\"");
        if (properties < 0) {
            return Collections.emptySet();
        }
        int open = text.indexOf('[', properties);
        if (open < 0) {
            return Collections.emptySet();
        }
        String array = text.substring(open + 1, matchingBracket(text, open));
        Set<String> names = new TreeSet<>();
        Matcher matcher = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"").matcher(array);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static int matchingBracket(String text, int open) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        throw new IllegalStateException("properties 数组未闭合");
    }
}
