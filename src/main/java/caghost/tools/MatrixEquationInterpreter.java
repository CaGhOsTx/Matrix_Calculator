package caghost.tools;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MatrixEquationInterpreter {
    private final ArrayList<Matrix> matrices;
    private final String expression;
    private final HashMap<String, Matrix> temporaryMatrices = new HashMap<>();
    private int next = 0;

    private MatrixEquationInterpreter(ArrayList<Matrix> matrices, String expression) {
        this.matrices = matrices;
        this.expression = expression;
    }

    public static MatrixEquationInterpreter compile(ArrayList<Matrix> matrices, String expression) {
        expressionValidityCheck(expression);
        return new MatrixEquationInterpreter(matrices, expression);
    }

    private static void expressionValidityCheck(String expression) {
        if (unequalBrackets(expression) || expression.matches(".*[^ /*)(+^-][^ /*)(+^-].*"))
            throw new InvalidParameterException("Invalid expression");
    }

    private static boolean unequalBrackets(String expression) {
        int count = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(')
                count++;
            if (c == ')')
                count--;
        }
        return count != 0;
    }

    public Matrix solve() {
        return temporaryMatrices.get(convertExpressionToCode(expression));
    }

    private String convertExpressionToCode(String s) {
        String inner = innerMostExpression(s);
        String solved = add(subtract(multiply(scale(pow(inner)))));
        if (!s.equals(inner))
            return convertExpressionToCode(s.replace("(" + inner + ")", solved));
        if (s.matches(".+[+\\-*^].+"))
            return convertExpressionToCode(solved);
        return s;
    }

    private String pow(String s) {
        return pow(s, Pattern.compile("[A-Z]+\\d?\\s*\\^\\s*\\d+").matcher(s));
    }

    private String pow(String s, Matcher power) {
        if (power.find()) {
            String[] names = power.group().split("\\s*\\^\\s*");
            temporaryMatrices.put("TEMP" + next, Matrix.pow(temporaryMatrices.containsKey(names[0]) ? temporaryMatrices.get(names[0]) : matrices.get(names[0].charAt(0) - 'A'), names[1].charAt(0) - '0'));
            return pow(s.replace(power.group(), "TEMP" + next++));
        }
        return s;
    }

    private String multiply(String s) {
        return multiply(s, Pattern.compile("[A-Z]+\\d?\\s*\\*\\s*[A-Z]+\\d?").matcher(s));
    }

    private String multiply(String s, Matcher multiplication) {
        if (multiplication.find()) {
            String[] names = multiplication.group().split("\\s*\\*\\s*");
            var m1 = temporaryMatrices.containsKey(names[0]) ? temporaryMatrices.get(names[0]) : matrices.get(names[0].charAt(0) - 'A');
            var m2 = temporaryMatrices.containsKey(names[1]) ? temporaryMatrices.get(names[1]) : matrices.get(names[1].charAt(0) - 'A');
            temporaryMatrices.put("TEMP" + next, Matrix.multiply(m1, m2));
            return multiply(s.replace(multiplication.group(), "TEMP" + next++));
        }
        return s.trim();
    }

    private String scale(String s) {
        return scale(s, Pattern.compile("(?<![A-Z])\\d+\\s*\\*\\s*[A-Z]+\\d?|[A-Z]+\\d?\\s*\\*\\s*\\d+(?![A-Z])").matcher(s));
    }

    private String scale(String s, Matcher scaling) {
        if (scaling.find()) {
            String[] names = scaling.group().split("\\s*\\*\\s*");
            if (names[0].matches("\\d+"))
                temporaryMatrices.put("TEMP" + next, Matrix.scale(temporaryMatrices.containsKey(names[1]) ? temporaryMatrices.get(names[1]) : matrices.get(names[1].charAt(0) - 'A'), names[0].charAt(0) - '0'));
            else
                temporaryMatrices.put("TEMP" + next, Matrix.scale(temporaryMatrices.containsKey(names[0]) ? temporaryMatrices.get(names[0]) : matrices.get(names[1].charAt(0) - 'A'), names[1].charAt(0) - '0'));
            return scale(s.replace(scaling.group(), "TEMP" + next++));
        }
        return s;
    }

    private String add(String s) {
        return add(s, Pattern.compile("[A-Z]+\\d?\\s*\\+\\s*[A-Z]+\\d?").matcher(s));
    }

    private String add(String s, Matcher addition) {
        if (addition.find()) {
            String[] names = addition.group().split("\\s*\\+\\s*");
            Matrix m1 = temporaryMatrices.containsKey(names[0]) ? temporaryMatrices.get(names[0]) : matrices.get(names[0].charAt(0) - 'A');
            Matrix m2 = temporaryMatrices.containsKey(names[1]) ? temporaryMatrices.get(names[1]) : matrices.get(names[1].charAt(0) - 'A');
            temporaryMatrices.put("TEMP" + next, Matrix.add(m1, m2));
            return add(s.replace(addition.group(), "TEMP" + next++));
        }
        return s;
    }

    private String subtract(String s) {
        return subtract(s, Pattern.compile("[A-Z]+\\d?\\s*-\\s*[A-Z]+\\d?").matcher(s));
    }

    private String subtract(String s, Matcher subtraction) {
        if (subtraction.find()) {
            String[] names = subtraction.group().split("\\s*-\\s*");
            Matrix m1 = temporaryMatrices.containsKey(names[0]) ? temporaryMatrices.get(names[0]) : matrices.get(names[0].charAt(0) - 'A');
            Matrix m2 = temporaryMatrices.containsKey(names[1]) ? temporaryMatrices.get(names[1]) : matrices.get(names[1].charAt(0) - 'A');
            temporaryMatrices.put("TEMP" + next, Matrix.subtract(m1, m2));
            return subtract(s.replace(subtraction.group(), "TEMP" + next++));
        }
        return s;
    }

    private static String innerMostExpression(String s) {
        var brackets = Pattern.compile("(?<=(\\()).+?(?=(\\)))").matcher(s);
        if (brackets.find()) {
            return innerMostExpression(brackets.group());
        }
        return s;
    }
}
