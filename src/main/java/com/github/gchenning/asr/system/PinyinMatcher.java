package com.github.gchenning.asr.system;


import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼音相似度匹配器
 */
public class PinyinMatcher {
    private static final Logger logger = LoggerFactory.getLogger(PinyinMatcher.class);
    private List<Name> names = new ArrayList<Name>();

    /*public static void main(String[] args) {
        PinyinMatcher test = new PinyinMatcher();
        String[] names = {"李林锋", "魏永剑", "梅西", "西罗"};
        //logger.info(StringUtils.getLevenshteinDistance(pinyin1, pinyin2));
        for (String name : names) {
            test.addBase(name);
        }
        logger.info(test.suggest("c罗"));
    }*/

    public void addBase(String name) {
        names.add(generateName(name));
    }

    /**
     * 生成Name
     *
     * @param name
     * @return
     */
    private Name generateName(String name) {
        //拼音数组
        String[] pinyinNameArray = NameUtil.getPinyinNameArray(name);
        //拼音
        String pinyinName = "";
        for (String c1 : pinyinNameArray
                ) {
            pinyinName += c1;
        }
        //首字母
        String firstCharacterString = "";
        for (String pinyin : pinyinNameArray
                ) {
            firstCharacterString += pinyin.charAt(0);
        }
        //韵母
        String[] otherCharacter = new String[pinyinNameArray.length];
        int i = 0;
        for (String pinyin : pinyinNameArray
                ) {
            otherCharacter[i++] = pinyin.substring(1);
        }
        Name _name = new Name(name);
        _name.setPinyinNameArray(pinyinNameArray);
        _name.setPinyinName(pinyinName);
        _name.setPinyinFirstCharacter(firstCharacterString);
        _name.setPinyinOtherCharacters(otherCharacter);
        return _name;
    }

    /**
     * 获取相似度最大的名字
     *
     * @param chineseName
     * @return
     */
    public String suggest(String chineseName) {
        if (StringUtils.isBlank(chineseName) || chineseName.length() > 3 || chineseName.length() < 2) {
            return "";
        }
        Name _name1 = generateName(chineseName);
        //完全匹配
        for (Name name : names
                ) {
            if (_name1.getChineseName().contains(name.getChineseName())) {
                return name.getChineseName();
            }
        }
        //拼音匹配
        for (Name name : names
                ) {
            if (_name1.getPinyinName().contains(name.getPinyinName())) {
                return name.getChineseName();
            }
        }
        Map<Integer, Name> scoredName = new HashMap<Integer, Name>();
        //单字首拼音匹配+其他部分打分
        for (Name name : names
                ) {
            //相似度初始值为0
            int score = 0;
            //比较全部声母
            if (_name1.getPinyinFirstCharacter().equals(name.getPinyinFirstCharacter())) {
                score += 60;
            }
            //比较韵母
            for (int i = 0; _name1.getPinyinOtherCharacters().length == name.getPinyinOtherCharacters().length && i < _name1.getPinyinOtherCharacters().length; i++) {
                if (_name1.getPinyinOtherCharacters()[i].equals(name.getPinyinOtherCharacters()[i])) {
                    if (_name1.getPinyinOtherCharacters().length == 3) {
                        score += 10;
                    } else {
                        score += 20;
                    }
                }
            }
            //大于60分比较下一个
            if (score > 60) {
                scoredName.put(score, name);
                continue;
            }
            //分别比较声母
            for (int i = 0; _name1.getPinyinNameArray().length == name.getPinyinNameArray().length && i < _name1.getPinyinNameArray().length; i++) {
                if (_name1.getPinyinNameArray()[i].equals(name.getPinyinNameArray()[i])) {
                    if (_name1.getPinyinNameArray().length == 2) {
                        score += 30;
                    } else {
                        score += 20;
                    }
                }
            }
            scoredName.put(score, name);
        }
        //获取最大分数
        int max_score = 0;
        for (int _score : scoredName.keySet()) {
            if (max_score < _score) {
                max_score = _score;
            }
        }
        //没有相似的
        if (max_score == 0) {
            return "";
        }
        return scoredName.get(max_score).getChineseName();
    }

    static class NameUtil {
        /**
         * 获取拼音
         *
         * @param chineseName
         * @return
         */
        public static String getPinyinName(String chineseName) {
            String pinyin_name = "";
            char[] name_symbol = chineseName.toCharArray();
            String[] pinyin_name_symbol = new String[name_symbol.length];
            int i = 0;
            for (char symbol : name_symbol
                    ) {
                pinyin_name_symbol[i++] = PinyinHelper.toTongyongPinyinStringArray(symbol)[0];
            }
            for (String n_ : pinyin_name_symbol
                    ) {
                pinyin_name += n_;
            }
            return pinyin_name;
        }

        /**
         * 获取名字拼音数组
         *
         * @param chineseName
         * @return
         */
        public static String[] getPinyinNameArray(String chineseName) {
            char[] name_symbol = chineseName.toCharArray();
            String[] pinyin_name_symbol = new String[name_symbol.length];
            int i = 0;
            for (char symbol : name_symbol
                    ) {
                if (symbol >= 'A' && symbol <= 'Z' || symbol >= 'a' && symbol <= 'z') {
                    pinyin_name_symbol[i++] = symbol + "";
                } else {
                    pinyin_name_symbol[i++] = PinyinHelper.toTongyongPinyinStringArray(symbol)[0];
                }
            }
            return pinyin_name_symbol;
        }
    }

    /**
     * 人名拼音相似度比较对象
     */
    class Name {
        /**
         * 汉字名字
         */
        private String chineseName;
        /**
         * 拼音名字
         */
        private String pinyinName;
        /**
         * 拼音字母数组
         */
        private String[] pinyinNameArray;
        /**
         * 每个拼音首字母
         */
        private String pinyinFirstCharacter;
        /**
         * 每个拼音的韵母
         */
        private String[] pinyinOtherCharacters;

        public Name(String name) {
            this.chineseName = name;
        }

        public String[] getPinyinOtherCharacters() {
            return pinyinOtherCharacters;
        }

        public void setPinyinOtherCharacters(String[] pinyinOtherCharacters) {
            this.pinyinOtherCharacters = pinyinOtherCharacters;
        }

        public String getChineseName() {
            return chineseName;
        }

        public void setChineseName(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getPinyinName() {
            return pinyinName;
        }

        public void setPinyinName(String pinyinName) {
            this.pinyinName = pinyinName;
        }

        public String[] getPinyinNameArray() {
            return pinyinNameArray;
        }

        public void setPinyinNameArray(String[] pinyinNameArray) {
            this.pinyinNameArray = pinyinNameArray;
        }

        public String getPinyinFirstCharacter() {
            return pinyinFirstCharacter;
        }

        public void setPinyinFirstCharacter(String pinyinFirstCharacter) {
            this.pinyinFirstCharacter = pinyinFirstCharacter;
        }
    }
}
