package org.revo

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import groovy.transform.Canonical

import static Symbols.Symbol

class LexicalApplication {
    private static final String path = "C:\\Users\\ashraf\\Desktop\\lexical\\src\\main\\resources\\"
    private static Language language
    static {
        language = new Language(reserved: getReserved(path), symbols: getSymbols(path))
    }

    static void main(String[] args) {
        getTokens(path, language.symbols).each {
            println(it)
        }
    }

    static private List<String> getReserved(String path) {
        new File("${path}reserved").text.split("\t")
    }

    static private List<Symbols> getSymbols(String path) {
        new File("${path}symbols").readLines().collect {
            Symbol(it.split(":")[1].trim(), it.split(":")[0].trim().split(" ").toList().toSet())
        }
    }

    static private List<String> getTokens(String path, List<Symbols> symbolses) {
        String text = new File("${path}code").text.replace("\n", "").replace("\r", "")
        println(text)
        String s = symbolses*.values.join("")
        List<String> data = Splitter.on(CharMatcher.anyOf(s)).trimResults().omitEmptyStrings().splitToList(text)
        List<String> tokens = []
        char[] sp = s.toCharArray()
        int count = 0
        text = text.trim()
        while (text) {
            sp.each {
                if (text.startsWith(it as String)) {
                    tokens << (it as String)
                    text = text.replaceFirst("\\${it as String}", "").trim()
                }
            }
            if (!data[count]) {
                tokens.addAll(text.toCharArray().toList() as List<String>)
                break
            }
            String ob = data[count].trim()
            if (text.startsWith(ob)) {
                tokens << ob
                text = text.replaceFirst(ob, "").trim()
                count++
            }
        }
        getUpdatedTokens(tokens, symbolses)
    }

    static private List<String> getUpdatedTokens(List<String> oldTokens, List<Symbols> symbolsList) {
        List<String> tokens = []
        Set<String> symbols = symbolsList.collectMany { it.values }
        oldTokens.each {
            if (tokens && tokens.last()) {
                if (("${tokens.last()}$it" as String) in symbols)
                    tokens[tokens.size() - 1] = "${tokens.last()}$it"
                else tokens << (it as String)
            } else tokens << (it as String)
        }
        tokens
    }
}

@Canonical
class Symbols {
    String type
    Set<String> values

    static Symbols Symbol(String type, Set<String> values) {
        return new Symbols(type: type, values: values)
    }
}

@Canonical
class Language {
    List<String> reserved
    List<Symbols> symbols
}
