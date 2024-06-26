package com.github.tezvn.enchantic.impl.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {

	public static double getPercent(int number1, int number2, int scale) {
		return roundDouble(getPercent(number1, number2), scale);
	}

	public static double getPercent(int number1, int number2) {
		return Math.max(0, Math.min(number1, number2))*100.0f/number2;
	}

	public static double getPercent(double number1, double number2) {
		return Math.max(0, Math.min(number1, number2))*100.0f/number2;
	}

	public static double roundDouble(double amount) {
		return roundDouble(amount, 2);
	}

	public static double roundDouble(double amount, int scale) {
		return new BigDecimal(amount).setScale(Math.max(1, scale), RoundingMode.HALF_UP).doubleValue();
	}

	public static double convertToPercent(double value, int digit) {
		String str = String.valueOf(Math.max(0, value)/100);
		int index = 1;
		String[] split = str.split("");
		for (int i = 0; i < str.split("").length; i++) {
			if(split[i].equalsIgnoreCase(".")) {
				index = i;
				break;
			}
		}
		return Double.parseDouble(str.substring(0,
				digit == 0 ? index : Math.min(str.length(), index + digit + 1)));
	}

	public static int parseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return eval(str);
		}
	}

	public static int eval(final String str) {
		return (int) new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			//        | number | functionName factor | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if      (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if      (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
				}
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt")) x = Math.sqrt(x);
					else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
					else throw new RuntimeException("Unknown function: " + func);
				} else {
					throw new RuntimeException("Unexpected: " + (char)ch);
				}

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}
}
