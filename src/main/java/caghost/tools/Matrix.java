/*
Author: Carlos Milkovic
Date: 14.2.2021
Version: 1.0
*/

package caghost.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

public class Matrix {

    private final Fraction[][] values;
    private final int rows, columns;
    private final boolean isSquare;
    private Matrix inverse;

    /**
     * Class constructor. Creates an empty matrix of specified size.
     *
     * @param rows    number of rows.
     * @param columns number of columns.
     */
    public Matrix(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        isSquare = rows == columns;
        this.values = Fraction.loadEmptyArray(rows, columns);
    }

    /**
     * Class constructor. Creates a matrix with the values from the specified integer array.
     *
     * @param values - 2D rectangular array of integers.
     */
    public Matrix(int[][] values) {
        this.values = convertIntToFractionArray(values);
        this.rows = values.length;
        this.columns = values[0].length;
        isSquare = rows == columns;
    }

    /**
     * Class constructor. Creates a matrix with the values from the specified fraction array.
     *
     * @param values - 2D rectangular array of fractions.
     */
    public Matrix(Fraction[][] values) {
        this.values = values;
        rows = values.length;
        columns = values[0].length;
        isSquare = rows == columns;
    }

    private Matrix(Matrix A) {
        rows = A.rows;
        columns = A.columns;
        isSquare = rows == columns;
        values = new Fraction[A.values.length][];
        for (int i = 0; i < A.values.length; i++)
            values[i] = Arrays.copyOf(A.values[i], A.values[i].length);
        inverse = null;
    }

    /**
     * Returns a copy of the matrix.
     *
     * @return Matrix.
     */
    public Matrix clone() {
        return new Matrix(this);
    }

    public String toString() {
        var s = new StringBuilder();
        for (Fraction[] row : values) {
            for (Fraction f : row)
                s.append(" ").append(f.toString()).append(" ");
            s.append("\n");
        }
        return s.toString();
    }

    public String toHTML() {
        return "<html>" + this.toString().replaceAll("\n", "<br/>") + "</html>";
    }

    private Fraction[][] convertIntToFractionArray(int[][] array) {
        var fractions = new Fraction[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++)
                fractions[i][j] = new Fraction(array[i][j]);
        }
        return fractions;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Fraction[][] getValues() {
        return values;
    }

    public void print() { //prints the matrix to the console (for debugging)
        for (Fraction[] r : values) {
            for (Fraction f : r)
                System.out.printf("%5s", " " + f + " ");
            System.out.println();
        }
    }

    public void write(FileWriter w) throws IOException { //writes the matrix to a file (for debugging)
        for (Fraction[] r : values) {
            for (Fraction f : r)
                w.write(" " + f.toString() + " ");
            w.write("\n");
        }
        w.write("\n");
    }

    public static Matrix generate(int row, int column, int max, int min) { //a method that generates a matrix, with values ranging from min to max
        var rd = new Random();
        Fraction[][] values = new Fraction[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++)
                values[i][j] = new Fraction(rd.nextInt(max - min + 1) + min, 1);
        }
        return new Matrix(values);
    }

    public static Matrix add(Matrix A, Matrix B) {
        if (sameSize(A, B)) {
            Fraction[][] values = new Fraction[A.rows][A.columns];
            for (int i = 0; i < A.rows; i++) {
                for (int j = 0; j < A.columns; j++) {
                    values[i][j] = A.values[i][j].add(B.values[i][j]);
                }
            }
            return new Matrix(values);
        } else
            throw new InvalidParameterException("Cannot be added");
    }

    public static Matrix subtract(Matrix A, Matrix B) {
        return Matrix.add(A, scale(B, -1));
    }

    public static Matrix scale(Matrix A, int constant) {
        return scale(A, new Fraction(constant));
    }

    public static Matrix scale(Matrix A, Fraction constant) {
        Fraction[][] values = new Fraction[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                values[i][j] = A.values[i][j].multiply(constant);
            }
        }
        return new Matrix(values);
    }

    public static Matrix multiply(Matrix A, Matrix B) {
        if (A.columns == B.rows) {
            Fraction[][] values = new Fraction[A.rows][B.columns];
            for (int i = 0; i < A.rows; i++) {
                for (int j = 0; j < B.columns; j++) {
                    Fraction sum = new Fraction(0);
                    for (int k = 0; k < A.columns; k++)
                        sum = sum.add(A.values[i][k].multiply(B.values[k][j]));
                    values[i][j] = sum;
                }
            }
            return new Matrix(values);
        } else throw new InvalidParameterException("Cannot be multiplied");
    }

    public static Matrix pow(Matrix A, int power) {
        var product = A;
        for (int i = 1; i < power; i++)
            product = Matrix.multiply(A, product);
        return product;
    }

    public static Matrix rowEchelon(Matrix A) {
        A.properFormTest();
        Matrix REF = A.clone();
        if (REF.isSquare)
            REF.inverse = REF.generateIdentityMatrix();
        if(!REF.correctlySwapped())
            REF.orderLeadingOnes();
        for (int i = 0; i < REF.rows; i++) {
            for (int j = 0; i != 0 && j < (REF.isSquare ? REF.columns : REF.columns - 1); j++) {
                if (j < i && !REF.values[i][j].equals(Fraction.ZERO))
                    REF.subtractRowsMultipliedByConstant(i, j);
            }
            if (!REF.values[i][i].equals(Fraction.ONE)) {
                Fraction constant = REF.values[i][i];
                REF.divideRowByLeadingOneConstant(i, constant);
                if (REF.isSquare)
                    REF.inverse.divideRowByLeadingOneConstant(i, constant);
            }
        }
        return REF;
    }

    private void properFormTest() {
        if (rows != (isSquare ? columns : columns - 1))
            throw new InvalidParameterException("Improper form, no point solution");
        if (failsEmptyRowTest(true))
            throw new InvalidParameterException("empty row, no point solution");
        if (failsEmptyRowTest(false))
            throw new InvalidParameterException("empty column, no point solution");
        if (failsIdenticalRowTest())
            throw new InvalidParameterException("too many identical rows, no point solution");
    }

    public Matrix generateIdentityMatrix() {
        return Matrix.generateIdentityMatrix(this.rows, this.columns);
    }

    public static Matrix generateIdentityMatrix(int rows, int columns) {
        int[][] a = new int[rows][columns];
        if (rows != columns)
            throw new InvalidParameterException("Matrix is not square");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (j == i)
                    a[i][j] = 1;
                else
                    a[i][j] = 0;
            }
        }
        return new Matrix(a);
    }

    public Matrix inverse() {
        if (!isSquare)
            throw new InvalidParameterException("Not square");
        return (inverse != null) ? inverse : (inverse = reducedRowEchelon(this).inverse);
    }

    public static Matrix reducedRowEchelon(Matrix A) {
        var RREF = Matrix.rowEchelon(A);
        for (int currentRow = RREF.rows - 2; currentRow >= 0; currentRow--) {
            for (int i = RREF.rows - 1 - currentRow; i > 0; i--) {
                Fraction constant = RREF.values[currentRow][currentRow + i];
                if (!constant.equals(Fraction.ZERO)) {
                    RREF.subtractRowsMultipliedByConstant(currentRow, currentRow + i);
                }
            }
        }
        return RREF;
    }

    public Fraction determinant() {
        if(!isSquare)
            throw new ArithmeticException("Matrix not square");
        var temp = Matrix.rowEchelon(this.clone());
        Fraction sum = Fraction.ZERO;
        for(int i = 0; i < temp.values.length; i++)
            sum = sum.multiply(temp.values[i][i]);
        return sum;
    }

    private void divideRowByLeadingOneConstant(int index, Fraction constant) {
        for (int i = 0; i < columns; i++)
            values[index][i] = values[index][i].divide(constant);
    }

    private void subtractRowsMultipliedByConstant(int currentRow, int leadingRow) {
        Fraction constant = values[currentRow][leadingRow];
        for (int i = 0; i < columns; i++) {
            if (isSquare)
                inverse.values[currentRow][i] = inverse.values[currentRow][i].subtract(constant.multiply(inverse.values[leadingRow][i]));
            values[currentRow][i] = values[currentRow][i].subtract(constant.multiply(values[leadingRow][i]));
        }
        if (values[currentRow][currentRow].equals(Fraction.ZERO))
            throw new InvalidParameterException("Unsolvable, row " + currentRow + " contains a Zero leading variable");
    }

    /**
     * hard to refactor to be honest without making it look even worse.
     * basically, if both rows contain non zero values at the other row index eg row i and row j, row i must contain a non zero at j
     * also if the row only contains 1 non zero , a valid swapping row contains a non zero for that row, the column frequency of that other row can't be 1.
     * Hard to explain, took me three days of thinking lmao :D. You have to look at it kind of symmetrically, going over rows, and over columns of the diagonal[x][x] at the same time
     */
    private void orderLeadingOnes() {
        var solvedRows = new ArrayList<Integer>();
        int[] columnFrequencies = getColumnFrequencies();
        for (int i = 0; i < rows; i++) {
            int currentRowFrequency = rowNonZeroFrequency(i);
            if (rowDoesNotContainLeadingOne(i)) {
                for (int j = 0; j < (isSquare ? columns : columns - 1); j++) {
                    if (i == j || solvedRows.contains(j) || !rowContainsIndex(i, j)) {
                        continue;
                    }
                    if (currentRowFrequency == 1 && columnFrequencies[j] != 1) {
                        swapRows(i, j);
                        if (isSquare)
                            inverse.swapRows(i, j);
                        solvedRows.add(j);
                        if (values[i][i].equals(Fraction.ZERO))
                            i--;
                        break;
                    }
                    if (rowContainsIndex(j, i)) {
                        swapRows(j, i);
                        if (isSquare)
                            inverse.swapRows(j, i);
                        break;
                    }
                }
            }
        }
    }

    private int[] getColumnFrequencies() {
        int columns = this.isSquare ? this.columns : this.columns - 1;
        var columnFrequencies = new int[columns];
        for (int j = 0; j < columns; j++) {
            columnFrequencies[j] = this.columnNonZeroFrequency(j);
        }
        return columnFrequencies;
    }

    private int columnNonZeroFrequency(int column) {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            if (!values[i][column].equals(Fraction.ZERO))
                count++;
        }
        return count;
    }

    private int rowNonZeroFrequency(int row) {
        int count = 0;
        for (int i = 0; i < (isSquare ? columns : columns - 1); i++) {
            if (!values[row][i].equals(Fraction.ZERO))
                count++;
        }
        return count;
    }

    private boolean rowDoesNotContainLeadingOne(int row) {
        return values[row][row].equals(Fraction.ZERO);
    }

    private boolean rowContainsIndex(int row, int index) {
        return !values[row][index].equals(Fraction.ZERO);
    }

    private void swapRows(int row1, int row2) {
        Fraction[] tempRow = values[row1];
        values[row1] = values[row2];
        values[row2] = tempRow;
    }


    private boolean failsIdenticalRowTest() {
        return failsIdenticalRowTest(0);
    }

    private boolean failsIdenticalRowTest(int currentRow) { //throws an exception if at least n - (num 0s in row) rows are identical
        int identicalRows = 0;
        var currentZeroIndexes = buildNonZeroIndexList(currentRow, rows);
        for (int i = 0; i < rows; i++) {
            var tempZeroIndexes = buildNonZeroIndexList(i, isSquare ? columns : columns - 1);
            if (currentZeroIndexes.equals(tempZeroIndexes))
                identicalRows++;
        }
        if (identicalRows > rows - currentZeroIndexes.size()) // there can be at most n - (num 0s in row) identical rows.
            return true;
        if (currentRow < rows - 1)
            failsIdenticalRowTest(++currentRow);
        return false;
    }

    private ArrayList<Integer> buildNonZeroIndexList(int currentRow, int rows) {
        var list = new ArrayList<Integer>();
        for (int i = 0; i < rows; i++) {
            if (values[currentRow][i].equals(Fraction.ZERO))
                list.add(i);
        }
        return list;
    }

    /**
     * true for horizontal, false for vertical.
     */
    private boolean failsEmptyRowTest(boolean horizontalT_verticalF) {
        for (int i = 0; i < (isSquare ? columns : columns - 1); i++) {
            int count = 0;
            for (int j = 0; j < rows; j++) {
                if (horizontalT_verticalF)
                    count += (values[i][j].equals(Fraction.ZERO)) ? 1 : 0;
                else
                    count += (values[j][i].equals(Fraction.ZERO)) ? 1 : 0;
            }
            if (count == (isSquare ? columns : columns - 1))
                return true;
        }
        return false;
    }

    public boolean correctlySwapped() {
        for (int j = 0; j < getRows(); j++) {
            if (getValues()[j][j].equals(Fraction.ZERO))
                return false;
        }
        return true;
    }

    private static boolean sameSize(Matrix A, Matrix B) {
        return A.getRows() == B.getRows() && A.getColumns() == B.getColumns();
    }
}
