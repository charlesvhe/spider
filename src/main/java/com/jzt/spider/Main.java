package com.jzt.spider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by Charles on 2015/11/17.
 */
public class Main {




    public static void main(String[] args) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");


        System.out.println(engine.eval("class User{\n" +
                "        String name = \"Charles\";\n" +
                "    }\n" +
                "    User u = new User();"+
                "    System.out.println(u.name); return u;"));
    }
}
