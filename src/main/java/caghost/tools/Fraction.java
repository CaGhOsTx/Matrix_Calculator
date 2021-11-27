/*
Author: Carlos Milkovic
Date: 15.2.2021
Version: 1.0
*/
package caghost.tools;

import java.security.InvalidParameterException;

public final class Fraction extends Number implements Comparable<Fraction> {
    private final int numerator, denominator;
    private int hashCode = 0;
    public static final Fraction ONE = new Fraction(1), ZERO = new Fraction(0);

    public Fraction(int numerator) {
        this.numerator = numerator;
        this.denominator = 1;
    }

    /*
     * The class itself automatically converts fractions into simplest form and if negative it is always of form: -a / b
     */
    public Fraction(int numerator, int denominator) {
        if (denominator == 0) {
            if (numerator == 0) {
                throw new ArithmeticException("undefined");
            } else
                throw new ArithmeticException("division by zero");
        }
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        int gcd = greatestCommonDivisor(numerator, denominator);
        this.numerator = numerator / gcd;
        this.denominator = denominator / gcd;
    }

    public static Fraction[][] loadEmptyArray(int rows, int columns) {
        var array = new Fraction[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++)
                array[i][j] = new Fraction(0);
        }
        return array;
    }

    public static Fraction reciprocal(Fraction f) {
        if (f == null)
            throw new InvalidParameterException("Null fraction");
        return new Fraction(f.denominator, f.numerator);
    }

    /**
     * wikipedia, Euclid's algorithm.
     */
    private int greatestCommonDivisor(int numerator, int denominator) {
        if (numerator < 0)
            numerator = -numerator;
        while (denominator != 0) {
            int temp = numerator;
            numerator = denominator;
            denominator = temp % denominator;
        }
        return numerator;
    }

    @Override
    public int compareTo(Fraction f) {
        return numerator * f.denominator - f.numerator * denominator;
    }

    @Override
    public int intValue() {
        return numerator / denominator;
    }

    @Override
    public long longValue() {
        return numerator / denominator;
    }

    @Override
    public float floatValue() {
        return numerator / (float) denominator;
    }

    @Override
    public double doubleValue() {
        return numerator / (double) denominator;
    }

    public static Fraction convertString(String s) {
        if (s.matches("\\d+"))
            return new Fraction(Integer.parseInt(s));
        else if (s.matches("\\d+/\\d+")) {
            String[] f = s.split("/");
            return new Fraction(Integer.parseInt(f[0]), Integer.parseInt(f[1]));
        } else throw new InvalidParameterException("Inconvertible String");
    }

    @Override
    public String toString() {
        if (denominator == 1)
            return Integer.toString(numerator);
        else
            return numerator + "/" + denominator;
    }

    public boolean equals(int i) {
        if (denominator != 1)
            return false;
        return this.numerator == i;
    }

    public int hashCode() {
        if(hashCode != 0)
            return hashCode;
        else
            return hashCode = Integer.hashCode(numerator) + Integer.hashCode(denominator);
    }

    public boolean equals(float f) {
        return this.floatValue() == f;
    }

    public boolean equals(String s) {
        try {
            return this.equals(convertString(s));
        } catch (InvalidParameterException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Fraction))
            return false;
        Fraction f = (Fraction) o;
        return this.numerator == f.numerator && this.denominator == f.denominator;
    }

    public Fraction add(Fraction f) {
        if (f == null)
            throw new InvalidParameterException("Null fraction");
        int numerator = this.numerator * f.denominator + f.numerator * this.denominator;
        int denominator = this.denominator * f.denominator;
        if (numerator == 0)
            return new Fraction(0);
        return new Fraction(numerator, denominator);
    }

    public Fraction subtract(Fraction f) {
        if (f == null)
            throw new InvalidParameterException("Null fraction");
        int numerator = this.numerator * f.denominator - f.numerator * this.denominator;
        int denominator = this.denominator * f.denominator;
        if (numerator == 0)
            return new Fraction(0);
        return new Fraction(numerator, denominator);
    }

    public Fraction multiply(Fraction f) {
        int numerator = this.numerator * f.numerator;
        int denominator = this.denominator * f.denominator;
        if (numerator == 0)
            return new Fraction(0);
        return new Fraction(numerator, denominator);
    }

    public Fraction divide(Fraction f) {
        return this.multiply(Fraction.reciprocal(f));
    }
}


