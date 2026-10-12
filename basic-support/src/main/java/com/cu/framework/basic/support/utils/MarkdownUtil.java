package com.cu.framework.basic.support.utils;

import java.util.*;

/**
 * 工具源地址： https://github.com/kymotz/mdkille
 * bigTitle、title、subTitle、context text、context k-v text、
 * code block、link、table(matrix 2*3)、order list、un-order list
 *
 * @author liuqichun03
 * Created on 2022/11/8
 */
public class MarkdownUtil {

    // ~ APIs
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 构建入口，返回一个指向根节点的构建器，后续通过链式调用逐段追加内容，最后调用
     * {@link SectionBuilder#build()} 输出完整的 Markdown 文本
     * @return 根构建器
     */
    public static SectionBuilder of() {
        return new SectionBuilder(new Section(Section.Type.NORMAL, null, null, null, 0));
    }

    // ~ public classes & public constants & public enums
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 文本样式，用于修饰一段文本的输出形态；由 {@link Fonts#parseStyle} 转换为具体的 Markdown / HTML 片段
     */
    public enum Style {
        /** 普通文本，不加任何修饰；一旦样式集合中包含它，{@link Fonts#toString()} 会直接返回原文 */
        NORMAL("normal"),
        /** 加粗，输出 {@code **text**} */
        BOLD("bold"),
        /** 斜体，输出 {@code *text*} */
        ITALIC("italic"),
        /** 红色，输出 {@code <font color='red'>text</font>} */
        RED("red"),
        /** 绿色，输出 {@code <font color='green'>text</font>} */
        GREEN("green"),
        /** 灰色。注意：{@link Fonts#parseStyle} 的 switch 未覆盖此值，实际输出不会有任何修饰 */
        GRAY("gray"),
        /** 黄色，输出 {@code <font color='gold'>text</font>} */
        YELLOW("gold"),
        /** 蓝色，输出 {@code <font color='blue'>text</font>} */
        BLUE("blue");

        /** 样式名，颜色类样式会作为 {@code <font color='...'>} 的取值直接写入输出 */
        private final String name;

        Style(String name) {
            this.name = name;
        }

        /**
         * @return 样式名
         */
        public String getName() {
            return name;
        }
    }

    /**
     * 一段带样式的文本片段，是构建 Markdown 内容的最小单位；{@link #toString()} 时按所持样式集合渲染成最终文本
     */
    public static class Fonts {
        /** 空文本常量，用作"没有内容"的占位，避免传 null */
        public static final Fonts EMPTY = Fonts.of("");

        /** 文本原文 */
        private final String text;
        // ~ private fields
        // -------------------------------------------------------------------------------------------------------------
        /** 作用于该文本的样式集合；未指定样式时默认为 {@link Style#NORMAL} */
        private Set<Style> styles = Collections.emptySet();

        private Fonts(String text, Style... style) {
            this.text = text != null ? text : "";
            if (style != null) {
                this.styles = new HashSet<>(Arrays.asList(style));
            }
        }

        // ~ public methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 创建一段无修饰的普通文本，text 为 null 时按空串处理
         * @param text 文本原文
         * @return 文本片段
         */
        public static Fonts of(String text) {
            return new Fonts(text, Style.NORMAL);
        }

        /**
         * 创建一段带样式的文本，text 为 null 时按空串处理
         * @param text 文本原文
         * @param style 要叠加的样式，可传多个
         * @return 文本片段
         */
        public static Fonts of(String text, Style... style) {
            return new Fonts(text, style);
        }

        /**
         * @return 文本原文为 null 或空串时返回 true
         */
        public boolean isEmpty() {
            return this.text == null || this.text.isEmpty();
        }

        /**
         * 渲染成最终文本：样式集合中含 {@link Style#NORMAL} 时直接返回原文（其余样式被忽略），
         * 否则按集合中的每个样式依次包裹
         * @return 渲染后的文本
         */
        @Override
        public String toString() {
            if (styles.contains(Style.NORMAL)) {
                return text;
            }
            String last = text;
            for (Style style : styles) {
                last = parseStyle(last, style);
            }
            return last;
        }

        // ~ private methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 按指定样式包裹文本：BOLD 输出 {@code **text**}，ITALIC 输出 {@code *text*}，
         * RED/GREEN/BLUE/YELLOW 输出 {@code <font color='样式名'>text</font>}；
         * NORMAL、GRAY 以及 text/style 为 null 时原样返回
         * @param text 待包裹的文本
         * @param style 样式
         * @return 包裹后的文本
         */
        private String parseStyle(String text, Style style) {
            if (text == null || style == null) {
                return text;
            }
            switch (style) {
                case NORMAL:
                    break;
                case BOLD:
                    return "**" + text + "**";
                case ITALIC:
                    return "*" + text + "*";
                case RED:
                case GREEN:
                case BLUE:
                case YELLOW:
                    return "<font color='" + style.getName() + "'>" + text + "</font>";
            }
            return text;
        }
    }

    /**
     * 代表一行，可以是一个普通文本或一个K-V(s)数据
     */
    public static class MetaData {
        // ~ public constants
        // -------------------------------------------------------------------------------------------------------------
        /** K-V 行中键与值之间的分隔符 */
        public static final String DEFAULT_SEPARATOR = "：";
        /** K-V 行中多个值之间的分隔符 */
        public static final String DEFAULT_VALUE_SEPARATOR = " | ";
        /** 链接的输出模板，第一个占位为链接文字，第二个为 URL */
        public static final String LINK_TEMPLATE = "[%s▸](%s)";

        // ～ private fields
        // -------------------------------------------------------------------------------------------------------------
        /** 本行的类型，决定按普通文本、链接、链接列表还是换行来渲染 */
        private final Type type;
        /** 行首的键（或链接文字），为 null 表示本行只有值 */
        private final Fonts text;
        /** 行内的值集合；LINK 类型只取第一个作为 URL，LINK_LIST 类型按"文字, URL"成对解析 */
        private final Collection<Fonts> values;
        /** 键与值的分隔符 */
        private final String separator = DEFAULT_SEPARATOR;
        /** 多个值之间的分隔符 */
        private final String valueSeparator = DEFAULT_VALUE_SEPARATOR;

        /**
         * 构造一行只有普通文本、没有值的内容
         * @param text 文本
         */
        public MetaData(Fonts text) {
            this(text, null);
        }

        /**
         * 构造一行只有类型、没有文本和值的内容，用于 BR 这类不携带数据的行
         * @param type 行类型
         */
        public MetaData(Type type) {
            this(type, null, null);
        }

        /**
         * 构造一行普通类型（NORMAL）的 K-V 内容
         * @param text 键
         * @param values 值集合
         */
        public MetaData(Fonts text, Collection<Fonts> values) {
            this(Type.NORMAL, text, values);
        }

        /**
         * @param type 行类型
         * @param text 键（或链接文字）
         * @param values 值集合
         */
        public MetaData(Type type, Fonts text, Collection<Fonts> values) {
            this.type = type;
            this.text = text;
            this.values = values;
        }

        @Override
        public String toString() {
            return generateString(this.valueSeparator);
        }

        /**
         * generate one line
         */
        private String generateString(String valueSeparator) {
            boolean hasValues = values != null && !values.isEmpty();
            boolean hasText = text != null && !text.isEmpty();
            StringJoiner joiner = new StringJoiner(valueSeparator);
            String ret = "";
            switch (type) {
                case NORMAL:
                    if (hasText && hasValues) {
                        values.forEach(v -> joiner.add(v.toString()));
                        ret = text + separator + joiner;
                    } else if (!hasText && hasValues) {
                        values.forEach(v -> joiner.add(v.toString()));
                        ret = joiner.toString();
                    } else if (hasText) {
                        ret = text.toString();
                    }
                    break;
                case LINK:
                    if (hasText && hasValues) {
                        Fonts fonts = values.stream().findFirst().orElse(null);
                        if (fonts == null) {
                            break;
                        }
                        ret = String.format(LINK_TEMPLATE, text, fonts);
                    } else if (!hasText && hasValues) {
                        Fonts url = values.stream().findFirst().orElse(null);
                        if (url == null) {
                            break;
                        }
                        ret = String.format(LINK_TEMPLATE, url, url);
                    } else if (hasText) {
                        ret = String.format(LINK_TEMPLATE, text, text);
                    }
                    break;
                case LINK_LIST:
                    if (hasText && hasValues) {
                        ret = text + separator + generateLinkList(values);
                    } else if (!hasText && hasValues) {
                        ret = generateLinkList(values);
                    } else if (hasText) {
                        ret = String.format(LINK_TEMPLATE, text, text);
                    }
                    break;
                case BR:
                    ret = "<br>";
            }
            return ret;
        }

        // ~ private methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 将值集合按"链接文字, URL"两两配对渲染成链接列表，各项以 {@link #valueSeparator} 连接；
         * 若元素个数为奇数，最后一个元素同时作为链接文字和 URL
         * @param values 链接文字与 URL 交替排列的集合
         * @return 渲染后的链接列表文本，values 为空时返回空串
         */
        private String generateLinkList(Collection<Fonts> values) {
            if (values == null || values.isEmpty()) {
                return "";
            }
            Object[] valueArr = values.toArray();
            StringJoiner linkList = new StringJoiner(valueSeparator);
            for (int i = 0; i + 1 < valueArr.length; i += 2) {
                linkList.add(String.format(LINK_TEMPLATE, valueArr[i], valueArr[i + 1]));
            }
            boolean isPairNum = (valueArr.length % 2) == 0;
            if (!isPairNum) {
                String lastUrl = valueArr[valueArr.length - 1].toString();
                linkList.add(String.format(LINK_TEMPLATE, lastUrl, lastUrl));
            }
            return linkList.toString();
        }

        /**
         * 一行的渲染类型，决定 {@link #generateString} 如何拼装 text 与 values
         */
        private enum Type {
            /** only plain text, plain text list with a name */
            NORMAL,
            /**
             * text : link name
             * values: index 0 is URL if existed.
             */
            LINK,
            /**
             * text 为链接组名（可为空），values 按"链接文字, URL"交替排列，渲染成一行多个链接
             */
            LINK_LIST,
            /**
             * 换行，固定输出 {@code <br>}，text 与 values 均不参与渲染
             */
            BR,
        }
    }

    // ~ private class & private implements
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Markdown 文档树的一个节点。构建器每调用一次追加方法就往当前节点挂一个子节点，
     * 最终由 {@link MdParser} 自根向下递归渲染出完整文本
     */
    private static class Section {
        /** 缩进层级，列表嵌套时每深一层加 4 个空格 */
        private final int depth;
        /** 节点类型，决定渲染时套用哪种 Markdown 语法 */
        private Type type;
        /** 节点携带的数据：文本类节点为 {@link MetaData}，表格节点为 {@code Object[][]}，容器类节点为 null */
        private Object data;
        /** 父节点，根节点为 null */
        private Section parent;
        /** 子节点列表，惰性初始化；叶子节点为 null */
        private List<Section> children;

        private Section(Type type, Object data, Section parent, List<Section> children, int depth) {
            this.type = type;
            this.data = data;
            this.parent = parent;
            this.children = children;
            this.depth = depth;
        }

        // ~ public methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 追加一个子节点，首次调用时才创建子节点列表
         * @param child 子节点
         */
        public void addChild(Section child) {
            lazyInitChildren();
            children.add(child);
        }

        /**
         * @return 没有子节点列表或列表为空时返回 true
         */
        public boolean childIsEmpty() {
            return children == null || children.isEmpty();
        }

        // ~ private methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 按本节点类型渲染：容器类节点（REF / CODE / 列表 / TABLE）对子节点已渲染出的内容再套一层语法，
         * 文本类节点（NORMAL / 各级标题 / BR）则输出自身携带的数据
         * @param latestData 子节点已经渲染好的内容；对文本类节点该参数不参与输出
         * @return 渲染结果
         */
        private StringBuilder parse(StringBuilder latestData) {
            switch (type) {
                case LINK:
                case NORMAL:
                    latestData.append('\n').append(parseData(""));
                    return latestData;
                case BIG_TITLE:
                    latestData.append('\n').append(parseData("## "));
                    return latestData;
                case TITLE:
                    latestData.append('\n').append(parseData("### "));
                    return latestData;
                case SUBTITLE:
                    latestData.append('\n').append(parseData("#### "));
                    return latestData;
                case REF:
                    return parseRefSection(latestData);
                case CODE:
                    StringBuilder codeBlock = new StringBuilder(latestData.length() + 10);
                    codeBlock.append("\n```").append(latestData).append("\n```");
                    return codeBlock;
                case ORDER_LIST:
                    return parseOrderListSection(latestData);
                case UN_ORDER_LIST:
                    return parseUnOrderListSection(latestData);
                case TABLE:
                    return parseTableSection(latestData);
                case BR:
                    return latestData.append(parseData(""));
            }
            return latestData;
        }

        /**
         * 输出本节点数据并加上前缀，data 为 null 时返回空串
         * @param prefix 语法前缀，如标题的 {@code "## "}
         * @return 前缀 + 数据
         */
        private String parseData(String prefix) {
            if (data == null) {
                return "";
            }
            return prefix + data;
        }

        /**
         * 渲染引用块：给内容的每一行加上 {@code > } 前缀（首行不以换行开头时同样补前缀）
         * @param latestData 子节点已渲染好的内容
         * @return 加了引用前缀的内容；内容为空时原样返回
         */
        private StringBuilder parseRefSection(StringBuilder latestData) {
            char[] chars = latestData.toString().toCharArray();
            if (chars.length <= 0) {
                return latestData;
            }
            StringBuilder data = new StringBuilder(chars.length * 2);
            if (chars[0] != '\n') {
                data.append("> ");
            }
            char last = 0;
            for (char c : chars) {
                if (last == '\n') {
                    data.append("> ");
                }
                data.append(c);
                last = c;
            }
            return data;
        }

        /**
         * 渲染有序列表：给内容的每一行加上 {@code 1. }、{@code 2. } … 递增序号，
         * 并按 depth 补 4 空格/层的缩进；空行与已带空格的行不再编号
         * @param latestData 子节点已渲染好的内容
         * @return 编号后的内容；内容为空时原样返回
         */
        private StringBuilder parseOrderListSection(StringBuilder latestData) {
            char[] chars = latestData.toString().toCharArray();
            if (chars.length <= 0) {
                return latestData;
            }
            StringBuilder data = new StringBuilder(chars.length * 2);
            String padding = String.join("", Collections.nCopies(depth * 4, " "));
            int order = 1;
            if (chars[0] != '\n') {
                data.append(padding).append(order++).append(". ");
            }
            char last = 0;
            for (char c : chars) {
                if (last == '\n' && c != '\n' && c != ' ') {
                    data.append(padding).append(order++).append(". ");
                }
                data.append(c);
                last = c;
            }
            return data;
        }

        /**
         * 渲染无序列表：给内容的每一行加上 {@code - } 前缀，并按 depth 补 4 空格/层的缩进；
         * 空行与已带空格的行不再添加
         * @param latestData 子节点已渲染好的内容
         * @return 加了列表符号的内容；内容为空时原样返回
         */
        private StringBuilder parseUnOrderListSection(StringBuilder latestData) {
            char[] chars = latestData.toString().toCharArray();
            if (chars.length <= 0) {
                return latestData;
            }
            StringBuilder data = new StringBuilder(chars.length * 2);
            String padding = String.join("", Collections.nCopies(depth * 4, " "));
            if (chars[0] != '\n') {
                data.append(padding).append("- ");
            }
            char last = 0;
            for (char c : chars) {
                if (last == '\n' && c != '\n' && c != ' ') {
                    data.append(padding).append("- ");
                }
                data.append(c);
                last = c;
            }
            return data;
        }

        /**
         * 渲染表格：data 为二维数组，第 0 行作为表头，其后自动生成一行 {@code -} 分隔符，
         * 余下每行作为数据行，单元格以 {@code " | "} 连接，null 单元格输出空串
         * @param latestData 已渲染的前序内容，表格追加在其后（表格前后各补换行）
         * @return 追加了表格的内容；data 为 null 或空数组时只补一个换行
         */
        private StringBuilder parseTableSection(StringBuilder latestData) {
            if (data != null) {
                Object[][] tableData = (Object[][]) data;
                if (tableData.length > 0 && tableData[0].length > 0) {
                    StringJoiner titles = new StringJoiner(" | "), extras = new StringJoiner(" | ");
                    for (Object t : tableData[0]) {
                        titles.add(t != null ? t.toString() : "");
                        extras.add("-");
                    }
                    latestData.append("\n\n").append(titles).append('\n').append(extras);
                    for (int i = 1; i < tableData.length; i++) {
                        StringJoiner dataJoiner = new StringJoiner(" | ");
                        for (int j = 0; j < tableData[i].length; j++) {
                            dataJoiner.add(tableData[i][j] != null ? tableData[i][j].toString() : "");
                        }
                        latestData.append('\n').append(dataJoiner);
                    }
                }
            }
            return latestData.append('\n');
        }

        private void lazyInitChildren() {
            if (children == null) {
                children = new ArrayList<>();
            }
        }

        // ~ getter & setter
        // -------------------------------------------------------------------------------------------------------------
        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Section getParent() {
            return parent;
        }

        public void setParent(Section parent) {
            this.parent = parent;
        }

        public List<Section> getChildren() {
            return children;
        }

        public void setChildren(List<Section> children) {
            this.children = children;
        }

        public int getDepth() {
            return depth;
        }

        /**
         * 节点类型，同时约定了 {@link Section#data} 的取值形态（见各常量注释）
         */
        private enum Type {
            /**
             * data is {@link MetaData} and plain text
             */
            NORMAL,

            /**
             * data is {@link MetaData} and h2
             */
            BIG_TITLE,

            /**
             * data is {@link MetaData} and h3
             */
            TITLE,

            /**
             * data is {@link MetaData} and h4
             */
            SUBTITLE,

            /**
             * data is {@code null}, content is children
             */
            REF,

            /**
             * data is {@code null}, content is children
             */
            CODE,

            /**
             * data is matrix, aka String[][]
             */
            TABLE,

            /**
             * data is {@code null}, content is children
             */
            ORDER_LIST,

            /**
             * data is {@code null}, content is children
             */
            UN_ORDER_LIST,

            /**
             * data is {@link MetaData}
             */
            LINK,

            /**
             * data is {@link MetaData} of type {@link MetaData.Type#BR}, renders {@code <br>} without a leading newline
             */
            BR
        }
    }

    /**
     * Markdown 内容的链式构建器，通过 {@link MarkdownUtil#of()} 获得根构建器。
     * <p>普通追加方法（text / bigTitle / title / subTitle / link / links / br）向当前节点挂一个子节点并返回自身，
     * 可以一直链式调用；作用域方法（ref / ol / ul / code）会返回一个指向新子节点的<b>新构建器</b>，
     * 其后的追加都落在该子节点内，必须调用对应的 endRef / endOl / endUl / endCode 才能回到上层。
     * table() 则返回 {@link TableDataBuilder}，以 endTable() 结束。</p>
     * <p>{@link #build()} 会先回溯到根节点再整树渲染，因此在链上任意一个构建器调用都能得到完整结果。</p>
     */
    public static class SectionBuilder {
        /** 渲染器，无内部状态，所有构建器共享 */
        private static final MdParser parser = new MdParser();
        /**
         * first is root
         */
        private final Section curSec;
        /**
         * code, ref curr -> par
         */
        private Section parentSec;
        /**
         * init null
         */
        private SectionBuilder parentBuilder;

        private SectionBuilder(Section curSec) {
            this.curSec = curSec;
        }

        private SectionBuilder(Section curSec, Section parentSec, SectionBuilder parentBuilder) {
            this.curSec = curSec;
            this.parentSec = parentSec;
            this.parentBuilder = parentBuilder;
        }

        // ~ public methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 追加一行普通文本，text 为 null 时不追加任何内容
         * @param text 文本内容
         * @return 当前构建器
         */
        public SectionBuilder text(String text) {
            return text(text, (String) null);
        }

        /**
         * 追加一行 K-V 文本，输出形如 {@code name：value}
         * @param name 键，为 null 时整个调用不追加任何内容
         * @param value 值，为 null 时只输出 name
         * @return 当前构建器
         */
        public SectionBuilder text(String name, String value) {
            if (name != null) {
                Collection<Fonts> values
                        = value != null ? Collections.singletonList(Fonts.of(value)) : Collections.emptyList();
                curSec.addChild(new Section(Section.Type.NORMAL,
                        new MetaData(MetaData.Type.NORMAL, Fonts.of(name, (Style) null), values),
                        curSec, null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 追加一行带样式的文本，text 为 null 时不追加任何内容
         * @param text 文本内容
         * @param style 要叠加的样式，可传多个
         * @return 当前构建器
         */
        public SectionBuilder text(String text, Style... style) {
            if (text != null) {
                curSec.addChild(new Section(Section.Type.NORMAL, new MetaData(Fonts.of(text, style)), curSec,
                        null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 追加一行由多个值组成的文本，无键名，各值以 {@code " | "} 连接；values 为 null 或空时不追加任何内容
         * @param values 值集合
         * @return 当前构建器
         */
        public SectionBuilder text(Collection<String> values) {
            if (values != null && !values.isEmpty()) {
                text(null, values);
            }
            return this;
        }

        /**
         * 追加一行"键 + 多个值"的文本，输出形如 {@code name：v1 | v2}，值使用默认样式
         * @param name 键
         * @param values 值集合，为 null 或空时退化为只输出 name
         * @return 当前构建器
         */
        public SectionBuilder text(String name, Collection<String> values) {
            if (values == null || values.size() <= 0) {
                return text(name);
            }
            return text(name, null, values);
        }

        /**
         * 追加一行"键 + 多个带样式值"的文本，输出形如 {@code name：v1 | v2}
         * @param name 键
         * @param valueStyle 作用于每个值的样式，为 null 时按 {@link Style#NORMAL} 处理
         * @param values 值集合，为 null 或空时退化为只输出 name
         * @return 当前构建器
         */
        public SectionBuilder text(String name, Style valueStyle, Collection<String> values) {
            if (values == null || values.size() <= 0) {
                return text(name);
            }
            if (valueStyle == null) {
                valueStyle = Style.NORMAL;
            }
            List<Fonts> ele = new ArrayList<>(values.size());
            for (String value : values) {
                ele.add(Fonts.of(value, valueStyle));
            }
            curSec.addChild(new Section(Section.Type.NORMAL, new MetaData(Fonts.of(name), ele), curSec, null,
                    curSec.getDepth()));
            return this;
        }

        /**
         * 追加一个二级标题，输出 {@code ## title}
         * @param title 标题文字，为 null 时不追加任何内容
         * @return 当前构建器
         */
        public SectionBuilder bigTitle(String title) {
            if (title != null && title != "") {
                curSec.addChild(new Section(Section.Type.BIG_TITLE, new MetaData(Fonts.of(title)), curSec,
                        null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 追加一个三级标题，输出 {@code ### title}，使用默认样式
         * @param title 标题文字，为 null 时不追加任何内容
         * @return 当前构建器
         */
        public SectionBuilder title(String title) {
            return title(title, Style.NORMAL);
        }

        /**
         * 追加一个带样式的三级标题，输出 {@code ### title}
         * @param title 标题文字，为 null 时不追加任何内容
         * @param color 作用于标题文字的样式
         * @return 当前构建器
         */
        public SectionBuilder title(String title, Style color) {
            if (title != null && title != "") {
                curSec.addChild(new Section(Section.Type.TITLE, new MetaData(Fonts.of(title, color)),
                        curSec, null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 追加一个带标签的三级标题，标签以 {@code " | "} 连接输出在标题文字之后
         * @param title 标题文字，为 null 时不追加任何内容
         * @param label 标题后的标签片段
         * @return 当前构建器
         */
        public SectionBuilder title(String title, Fonts... label) {
            return title(title, null, label);
        }

        /**
         * 追加一个带样式和标签的三级标题，输出形如 {@code ### title：label1 | label2}
         * @param title 标题文字，为 null 时不追加任何内容
         * @param titleColor 作用于标题文字的样式，为 null 时按 {@link Style#NORMAL} 处理
         * @param label 标题后的标签片段，为 null 时按空列表处理
         * @return 当前构建器
         */
        public SectionBuilder title(String title, Style titleColor, Fonts... label) {
            if (title != null && title != "") {
                if (titleColor == null) {
                    titleColor = Style.NORMAL;
                }
                List<Fonts> labelList = label != null ? Arrays.asList(label) : Collections.emptyList();
                curSec.addChild(new Section(Section.Type.TITLE, new MetaData(Fonts.of(title, titleColor), labelList),
                        curSec, null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 追加一个四级标题，输出 {@code #### title}
         * @param title 标题文字，为 null 时不追加任何内容
         * @return 当前构建器
         */
        public SectionBuilder subTitle(String title) {
            if (title != null  && title != "") {
                curSec.addChild(new Section(Section.Type.SUBTITLE, new MetaData(Fonts.of(title)),
                        curSec, null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 开启一个引用块作用域，返回指向该引用块的新构建器；块内追加的每一行都会加上 {@code > } 前缀。
         * 必须以 {@link #endRef()} 结束才能回到上层继续追加
         * @return 引用块内的子构建器
         */
        public SectionBuilder ref() {
            Section refSection = new Section(Section.Type.REF, null, curSec, new ArrayList<>(), curSec.getDepth());
            curSec.addChild(refSection);
            return new SectionBuilder(refSection, curSec, this);
        }

        /**
         * 结束引用块作用域，回到创建它的上层构建器；在根构建器上调用则返回自身
         * @return 上层构建器
         */
        public SectionBuilder endRef() {
            return this.parentBuilder != null ? this.parentBuilder : this;
        }

        /**
         * 开启一个表格作用域
         * @return 表格数据构建器，填完数据后需调用 {@link TableDataBuilder#endTable()} 回到本构建器
         */
        public TableDataBuilder table() {
            return new TableDataBuilder(curSec, this);
        }

        /**
         * 追加一个链接，链接文字与 URL 相同，输出 {@code [url▸](url)}
         * @param url 链接地址，为 null 或空串时不追加任何内容
         * @return 当前构建器
         */
        public SectionBuilder link(String url) {
            return link(null, url);
        }

        /**
         * 追加一个带文字的链接，输出 {@code [name▸](url)}
         * @param name 链接文字，为 null 或空串时改用 url 作为文字
         * @param url 链接地址，为 null 或空串时不追加任何内容
         * @return 当前构建器
         */
        public SectionBuilder link(String name, String url) {
            if (name == null || name == "") {
                name = url;
            }
            if (url != null && url != "") {
                MetaData links = new MetaData(MetaData.Type.LINK, Fonts.of(name),
                        Collections.singletonList(Fonts.of(url)));
                curSec.addChild(new Section(Section.Type.NORMAL, links, curSec, null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 追加一行多个链接，无组名
         * @param urlMappings 链接映射，key 为链接文字、value 为 URL；为 null 或空时不追加任何内容
         * @return 当前构建器
         */
        public SectionBuilder links(Map<String, String> urlMappings) {
            return links(null, urlMappings);
        }

        /**
         * 追加一行"组名 + 多个链接"，输出形如 {@code name：[k1▸](v1) | [k2▸](v2)}，链接顺序取决于 Map 的迭代顺序
         * @param name 链接组名，为 null 或空串时不输出组名
         * @param urlMappings 链接映射，key 为链接文字、value 为 URL；为 null 或空时不追加任何内容
         * @return 当前构建器
         */
        public SectionBuilder links(String name, Map<String, String> urlMappings) {
            if (urlMappings != null && !urlMappings.isEmpty()) {
                List<Fonts> serialUrlInfos = new ArrayList<>();
                for (Map.Entry<String, String> entry : urlMappings.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    serialUrlInfos.add(Fonts.of(key != null ? key : ""));
                    serialUrlInfos.add(Fonts.of(value != null ? value : ""));
                }
                Fonts wrappedName = name != null && name != "" ? Fonts.of(name) : Fonts.EMPTY;
                MetaData linksGroup = new MetaData(MetaData.Type.LINK_LIST, wrappedName, serialUrlInfos);
                curSec.addChild(new Section(Section.Type.NORMAL, linksGroup, curSec, null, curSec.getDepth()));
            }
            return this;
        }

        /**
         * 开启一个有序列表作用域，返回指向该列表的新构建器；块内追加的每一行都会加上递增序号。
         * 在另一个列表作用域内调用时缩进层级 +1，形成嵌套列表。必须以 {@link #endOl()} 结束
         * @return 有序列表内的子构建器
         */
        public SectionBuilder ol() {
            int depth = (curSec.getType() == Section.Type.ORDER_LIST || curSec.getType() == Section.Type.UN_ORDER_LIST)
                    ? curSec.getDepth() + 1
                    : curSec.getDepth();
            Section OrderListSec = new Section(Section.Type.ORDER_LIST, null, curSec, new ArrayList<>(), depth);
            curSec.addChild(OrderListSec);
            return new SectionBuilder(OrderListSec, curSec, this);
        }

        /**
         * 结束有序列表作用域，回到创建它的上层构建器；在根构建器上调用则返回自身
         * @return 上层构建器
         */
        public SectionBuilder endOl() {
            return this.parentBuilder != null ? this.parentBuilder : this;
        }

        /**
         * 开启一个无序列表作用域，返回指向该列表的新构建器；块内追加的每一行都会加上 {@code - } 前缀。
         * 在另一个列表作用域内调用时缩进层级 +1，形成嵌套列表。必须以 {@link #endUl()} 结束
         * @return 无序列表内的子构建器
         */
        public SectionBuilder ul() {
            int depth = (curSec.getType() == Section.Type.ORDER_LIST || curSec.getType() == Section.Type.UN_ORDER_LIST)
                    ? curSec.getDepth() + 1
                    : curSec.getDepth();
            Section unOrderListSec = new Section(Section.Type.UN_ORDER_LIST, null, curSec, new ArrayList<>(), depth);
            curSec.addChild(unOrderListSec);
            return new SectionBuilder(unOrderListSec, curSec, this);
        }

        /**
         * 结束无序列表作用域，回到创建它的上层构建器；在根构建器上调用则返回自身
         * @return 上层构建器
         */
        public SectionBuilder endUl() {
            return this.parentBuilder != null ? this.parentBuilder : this;
        }

        /**
         * 开启一个代码块作用域，返回指向该代码块的新构建器；块内追加的内容整体被 ``` 围栏包裹。
         * 必须以 {@link #endCode()} 结束
         * @return 代码块内的子构建器
         */
        public SectionBuilder code() {
            Section codeSec = new Section(Section.Type.CODE, null, curSec, new ArrayList<>(), curSec.getDepth());
            curSec.addChild(codeSec);
            return new SectionBuilder(codeSec, curSec, this);
        }

        /**
         * 结束代码块作用域，回到创建它的上层构建器；在根构建器上调用则返回自身
         * @return 上层构建器
         */
        public SectionBuilder endCode() {
            return this.parentBuilder != null ? this.parentBuilder : this;
        }

        /**
         * 追加一个 HTML 换行标记 {@code <br>}，不产生 Markdown 段落换行
         * @return 当前构建器
         */
        public SectionBuilder br() {
            curSec.addChild(new Section(Section.Type.BR, new MetaData(MetaData.Type.BR), parentSec, null,
                    curSec.getDepth()));
            return this;
        }

        /**
         * 从根节点开始渲染整棵文档树，输出最终的 Markdown 文本（首尾空白已去除）。
         * 在链上任意一个构建器调用效果相同
         * @return 完整的 Markdown 字符串
         */
        public String build() {
            return parser.parse(curSec);
        }
    }

    /**
     * 表格数据构建器，由 {@link SectionBuilder#table()} 获得；填入数据后调用 {@link #endTable()}
     * 把表格节点挂回父节点并返回上层的 {@link SectionBuilder}
     */
    public static class TableDataBuilder {
        /** 表格要挂载到的父节点 */
        private final Section parentSec;
        /** 结束表格后要返回的构建器 */
        private final SectionBuilder parentBuilder;
        /** 表格数据，第 0 行为表头；未成功赋值时为 null，渲染结果只剩一个换行 */
        private Object[][] tableData;

        private TableDataBuilder(Section parentSec, SectionBuilder parentBuilder) {
            this.parentSec = parentSec;
            this.parentBuilder = parentBuilder;
        }

        // ~ public methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 直接以二维数组作为整张表格，第 0 行会被当作表头渲染并自动补一行 {@code -} 分隔符
         * @param table 表格数据，为 null 或空数组时忽略本次调用
         * @return 当前构建器
         */
        public TableDataBuilder data(Object[][] table) {
            if (table != null && table.length > 0 && table[0].length > 0) {
                tableData = table;
            }
            return this;
        }

        /**
         * 分别指定表头与数据行，内部拼成一张二维表格。
         * 列数取 {@code title.length} 与 {@code data[0].length} 的较小值，超出部分被截断
         * @param title 表头，为 null 时等价于直接调用 {@link #data(Object[][])}
         * @param data 数据行，为 null 或空数组时本次调用不产生任何数据
         * @return 当前构建器
         */
        public TableDataBuilder data(Object[] title, Object[][] data) {
            if (title == null && data != null) {
                return data(data);
            }
            if (data != null && data.length > 0 && data[0].length > 0) {
                int minCol = Math.min(title.length, data[0].length);
                tableData = new Object[data.length + 1][minCol];
                tableData[0] = Arrays.copyOfRange(title, 0, minCol);
                for (int i = 0; i < data.length; i++) {
                    tableData[i + 1] = Arrays.copyOfRange(data[i], 0, minCol);
                }
            }
            return this;
        }

        /**
         * 结束表格：把已填入的数据作为表格节点挂到父节点上
         * @return 创建本构建器的 {@link SectionBuilder}，可继续链式追加
         */
        public SectionBuilder endTable() {
            parentSec.addChild(new Section(Section.Type.TABLE, tableData, parentSec, null, parentSec.getDepth()));
            return parentBuilder;
        }
    }

    /**
     * 文档树渲染器，把 {@link Section} 树递归展开成 Markdown 文本；无内部状态
     */
    private static class MdParser {
        // ~ public methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 渲染入口：先回溯到根节点，再从根开始整树渲染
         * @param sec 任意一个节点，不必是根
         * @return 渲染结果，首尾空白已去除
         */
        public String parse(Section sec) {
            Section root = findRoot(sec);
            return doParse(root, root).toString().trim();
        }

        // ~ private methods
        // -------------------------------------------------------------------------------------------------------------

        /**
         * 沿 parent 链向上回溯找到根节点
         * @param sec 起始节点
         * @return parent 为 null 的那个节点
         */
        private Section findRoot(Section sec) {
            if (sec.getParent() == null) {
                return sec;
            }
            return findRoot(sec.getParent());
        }

        /**
         * 深度优先渲染：叶子节点直接渲染自身，非叶子节点先把所有子节点的渲染结果拼接起来，
         * 再交给自身按类型套上对应语法
         * @param cur 当前节点
         * @param root 根节点，用于判断当前节点是否为根的直接子节点（是则在其后补一个换行）
         * @return 当前子树的渲染结果；cur 为 null 时返回 null
         */
        private StringBuilder doParse(Section cur, Section root) {
            if (cur == null) {
                return null;
            }
            if (cur.childIsEmpty()) {
                return cur.parse(new StringBuilder());
            }
            StringBuilder childData = new StringBuilder();
            for (Section child : cur.getChildren()) {
                StringBuilder part = doParse(child, root);
                if (part != null) {
                    childData.append(part);
                }
            }
            return cur.parse(childData).append(cur.getParent() == root ? '\n' : "");
        }
    }
}