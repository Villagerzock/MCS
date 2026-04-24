package net.villagerzock.compiler.gen;

import java.util.Stack;

public class PathStack extends Stack<String> {
    public String getPath(){
        StringBuilder builder = new StringBuilder();
        for (String s : this){
            builder.append(s);
            builder.append("/");
        }
        return builder.toString();
    }
}
