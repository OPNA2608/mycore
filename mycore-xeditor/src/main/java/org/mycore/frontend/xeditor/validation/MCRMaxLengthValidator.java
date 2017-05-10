package org.mycore.frontend.xeditor.validation;

public class MCRMaxLengthValidator extends MCRValidator {

    private int maxLength;

    @Override
    public boolean hasRequiredAttributes() {
        return hasAttributeValue("maxLength");
    }

    @Override
    public void configure() {
        maxLength = Integer.parseInt(getAttributeValue("maxLength"));
    }

    @Override
    protected boolean isValid(String value) {
        return value.length() <= maxLength;
    }
}