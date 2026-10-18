package yudxx.kursh.ui;

import yudxx.kursh.core.JsonLite;

public class SaveField {

    public final String path;
    public final String kind;
    public final String text;
    public final int start;
    public final int end;
    public String newText = "";

    public SaveField(JsonLite.Field field) {
        this.path = field.path;
        this.kind = field.kind;
        this.text = field.text;
        this.start = field.start;
        this.end = field.end;
        this.newText = field.text;
    }

    public boolean changed() {
        return newText != null && !newText.trim().isEmpty() && !newText.trim().equals(text);
    }

    public String typeText() {
        if ("bool".equals(kind)) return "bool";
        if ("int".equals(kind)) return "int";
        return "float";
    }
}
