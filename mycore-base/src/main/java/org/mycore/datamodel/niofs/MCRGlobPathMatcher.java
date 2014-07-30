/**
 *
 */
package org.mycore.datamodel.niofs;

import java.nio.file.FileSystem;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Thomas Scheffler
 *
 */
public class MCRGlobPathMatcher extends MCRPathMatcher {
    private static final String regexReservedChars = "^$.{()+*[]|";

    private static final String globReservedChars = "?*\\{[";

    private static char END_OF_PATTERN = 0;

    /**
     * A {@link PathMatcher} that accepts 'glob' syntax
     * See {@link FileSystem#getPathMatcher(String)} for 'glob' syntax
     * @param globPattern
     */
    public MCRGlobPathMatcher(final String globPattern) {
        super(globPattern);
    }

    private static int addCharacterClass(final StringBuilder regex, final String globPattern, int nextPos) {
        regex.append("[[^/]&&[");
        if (nextCharAt(globPattern, nextPos) == '^') {
            // escape the regex negation char if it appears
            regex.append("\\^");
            nextPos++;
        } else {
            // negation
            if (nextCharAt(globPattern, nextPos) == '!') {
                regex.append('^');
                nextPos++;
            }
            // hyphen allowed at start
            if (nextCharAt(globPattern, nextPos) == '-') {
                regex.append('-');
                nextPos++;
            }
        }
        boolean inRange = false;
        char rangeStartChar = 0;
        char curChar = '[';
        while (nextPos < globPattern.length()) {
            curChar = globPattern.charAt(nextPos++);
            if (curChar == ']') {
                break;
            }
            if (curChar == MCRAbstractFileSystem.SEPARATOR) {
                throw new PatternSyntaxException("Chracter classes cannot cross directory boundaries.", globPattern,
                    nextPos - 1);
            }
            if (curChar == '\\' || curChar == '[' || curChar == '&' && nextCharAt(globPattern, nextPos) == '&') {
                // escape '\', '[' or "&&"
                regex.append('\\');
            }
            regex.append(curChar);

            if (curChar == '-') {
                if (!inRange) {
                    throw new PatternSyntaxException("Invalid range.", globPattern, nextPos - 1);
                }
                if ((curChar = nextCharAt(globPattern, nextPos++)) == END_OF_PATTERN || curChar == ']') {
                    break;
                }
                if (curChar < rangeStartChar) {
                    throw new PatternSyntaxException("Invalid range.", globPattern, nextPos - 3);
                }
                regex.append(curChar);
                inRange = false;
            } else {
                inRange = true;
                rangeStartChar = curChar;
            }
        }
        if (curChar != ']') {
            throw new PatternSyntaxException("Missing ']'.", globPattern, nextPos - 1);
        }
        regex.append("]]");
        return nextPos;
    }

    private static String convertToRegex(final String globPattern) {
        boolean isInGroup = false;
        final StringBuilder regex = new StringBuilder("^");

        int nextPos = 0;
        while (nextPos < globPattern.length()) {
            final char c = globPattern.charAt(nextPos++);
            switch (c) {
            //Character handling
                case '\\':
                    nextPos = escapeCharacter(regex, globPattern, nextPos);
                    break;
                case '[':
                    nextPos = addCharacterClass(regex, globPattern, nextPos);
                    break;
                case '*':
                    nextPos = handleWildcard(regex, globPattern, nextPos);
                    break;
                case '?':
                    regex.append("[^/]");
                    break;
                //Group handling
                case '{':
                    isInGroup = startGroup(regex, globPattern, nextPos, isInGroup);
                    break;
                case '}':
                    isInGroup = endGroup(regex, isInGroup);
                    break;
                case ',':
                    if (isInGroup) {
                        regex.append(")|(?:");//separate values
                    } else {
                        regex.append(',');
                    }
                    break;
                default:
                    if (isRegexReserved(c)) {
                        regex.append('\\');
                    }
                    regex.append(c);
            }
        }

        if (isInGroup) {
            throw new PatternSyntaxException("Missing '}'.", globPattern, nextPos - 1);
        }

        final String regExp = regex.append('$').toString();
        return regExp;
    }

    private static boolean endGroup(final StringBuilder regex, boolean isInGroup) {
        if (isInGroup) {
            isInGroup = false;
            regex.append("))");
        } else {
            regex.append('}');
        }
        return isInGroup;
    }

    private static int escapeCharacter(final StringBuilder regex, final String globPattern, int nextPos) {
        if (nextPos == globPattern.length()) {
            throw new PatternSyntaxException("No character left to escape.", globPattern, nextPos - 1);
        }
        final char next = globPattern.charAt(nextPos++);
        if (isGlobReserved(next) || isRegexReserved(next)) {
            regex.append('\\');
        }
        regex.append(next);
        return nextPos;
    }

    private static int handleWildcard(final StringBuilder regex, final String globPattern, int nextPos) {
        if (nextCharAt(globPattern, nextPos) == '*') {
            //The ** characters matches zero or more characters crossing directory boundaries.
            regex.append(".*");
            nextPos++;
        } else {
            //The * character matches zero or more characters of a name component without crossing directory boundaries
            regex.append("[^/]*");
        }
        return nextPos;
    }

    private static boolean isGlobReserved(final char c) {
        return globReservedChars.indexOf(c) != -1;
    }

    private static boolean isRegexReserved(final char c) {
        return regexReservedChars.indexOf(c) != -1;
    }

    private static char nextCharAt(final String glob, final int i) {
        if (i < glob.length()) {
            return glob.charAt(i);
        }
        return END_OF_PATTERN;
    }

    private static boolean startGroup(final StringBuilder regex, final String globPattern, final int nextPos,
        final boolean isInGroup) {
        if (isInGroup) {
            throw new PatternSyntaxException("Nested groups are not supported.", globPattern, nextPos - 1);
        }
        regex.append("(?:(?:");
        return true;
    }

    @Override
    protected Pattern getPattern(final String globPattern) {
        final String regex = convertToRegex(globPattern);
        return Pattern.compile(regex);
    }

}