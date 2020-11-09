package me.gleeming.webapi.handler;

import lombok.Getter;

public class Handler {
    @Getter private final Class c;
    public Handler(Class c) { this.c = c; }
}
