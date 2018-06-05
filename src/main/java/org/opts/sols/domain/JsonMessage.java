package org.opts.sols.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JsonMessage {

    protected String text;

    public JsonMessage() {
    }

    public JsonMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
