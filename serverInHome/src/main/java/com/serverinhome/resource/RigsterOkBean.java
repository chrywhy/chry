package com.serverinhome.resource;

import javax.ejb.Stateless;

@Stateless
public class RigsterOkBean {
    public String sayHello(String name) {
        return "Hello, " + name + "!\n";
    }
}
