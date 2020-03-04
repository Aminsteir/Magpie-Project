package com.amin;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * The type Magpie 2.
 */
public class Magpie {
	/**
	 * The Keywords.
	 */
	private List<Pair<Integer,String>> keywords;
	/**
	 * The Responses.
	 */
	private List<Pair<Integer,String>> responses;
	/**
	 * The Input.
	 */
	private Scanner input;
	/**
	 * The Keywords writer.
	 */
	private BufferedWriter keywordsWriter;
	/**
	 * The Responses writer.
	 */
	private BufferedWriter responsesWriter;

	/**
	 * Instantiates a new Magpie 2.
	 *
	 * @param input the input
	 */
	public Magpie(Scanner input) {
		this.input = input;
		init();
	}

	/**
	 * Instantiates a new Magpie 2.
	 */
	public Magpie() {
		this.input = new Scanner(System.in);
		init();
	}

	/**
	 * Init.
	 */
	private void init() {
		this.keywords = new ArrayList<>();
		this.responses = new ArrayList<>();

		File keywordsTxt = new File("src/com/amin/keywords.txt");
		File responsesTxt = new File("src/com/amin/responses.txt");

		try {
			BufferedReader reader = new BufferedReader(new FileReader(keywordsTxt));
			String line = reader.readLine();
			while (line != null) {
				String[] arr = line.split("=");
				Integer associated = Integer.parseInt(arr[0]);
				String rest = arr[1];
				keywords.add(new Pair<>(associated, rest));
				line = reader.readLine();
			}

			reader = new BufferedReader(new FileReader(responsesTxt));
			line = reader.readLine();
			while (line != null) {
				String[] arr = line.split("=");
				int associated = Integer.parseInt(arr[0]);
				String rest = arr[1];
				responses.add(new Pair<>(associated, rest));
				line = reader.readLine();
			}

			this.keywordsWriter = new BufferedWriter(new FileWriter(keywordsTxt, true));
			this.responsesWriter = new BufferedWriter(new FileWriter(responsesTxt, true));
		} catch (IOException ignored) {}
	}

	/**
	 * Gets greeting.
	 *
	 * @return the greeting
	 */
	public String getGreeting() {
		return "Hello, let's talk.";
	}

	/**
	 * Gets response.
	 *
	 * @param statement the statement
	 * @return the response
	 */
	public String getResponse(String statement) {
		String response = "continue...";

		statement = statement.trim();

		if (statement.length() == 0)
			response = "Please say something";

		boolean foundKeyword = false;

		for (Pair<Integer, String> entry : keywords) {
			if (foundKeyword)
				break;

			String[] keywordsInPlace = entry.getValue().split("\\|");

			for (String keyword : keywordsInPlace) {
				if (keyword.trim().substring(keyword.length() - 2).equals("%s"))
					keyword = keyword.trim().substring(0,keyword.length() - 2);
				if (findKeyword(keyword, "%s", 0) >= 0) {
					// THIS PART REQUIRES A %S TO BE IN THE CENTER OF TWO PIECES OF TEXT
					String[] parts = keyword.split(" %s ");
					String firstPart = parts[0];
					String secondPart = parts[1];
					int firstPartPos = findKeyword(statement, firstPart, 0);
					int secondPartPos = findKeyword(statement, secondPart, firstPartPos);
					if (firstPartPos >= 0 && secondPartPos >= 0) {
						String stuffInMiddle = statement.substring(firstPartPos + firstPart.length() + 1, secondPartPos).trim();
						String tempResponse = Objects.requireNonNull(getPair(responses, entry.getKey())).getValue();
						String[] readyResponses = tempResponse.split("\\|");
						String pickedResponse = readyResponses[new Random().nextInt(readyResponses.length)];
						try {
							response = String.format(pickedResponse, stuffInMiddle);
						} catch (Exception e) {
							response = pickedResponse;
						}
						foundKeyword = true;
					}
				} else if (findKeyword(statement, keyword, 0) >= 0) {
					String tempResponse = Objects.requireNonNull(getPair(responses, entry.getKey())).getValue();

					// Randomly picks a response among the ready responses
					String[] responsesReady = tempResponse.split("\\|");
					String pickedResponse = responsesReady[new Random().nextInt(responsesReady.length)];

					if (findKeyword(pickedResponse, "%s", 0) >= 0) {
						int psn = findKeyword(statement, keyword, 0);
						String restOfStatement = statement.substring(psn + keyword.length()).trim();
						restOfStatement = restOfStatement.replaceAll("\\s*\\p{Punct}+\\s*$", "");
						pickedResponse = String.format(pickedResponse, restOfStatement);
					}

					response = pickedResponse;
					foundKeyword = true;
				}
			}
		}

		if (!foundKeyword) {
			try {
				addKeyword(statement);
			} catch (IOException ignored) {}
		}

		return response;
	}

	private Pair<Integer, String> getPair(List<Pair<Integer, String>> values, int keyValue) {
		for (Pair<Integer, String> pair: values) {
			if (pair.getKey() == keyValue) {
				return pair;
			}
		}
		return null;
	}

	private int getMaxKey(List<Pair<Integer, String>> values) {
		int max = values.get(0).getKey();
		for (Pair<Integer, String> pair: values) {
			if (pair.getKey() > max) {
				max = pair.getKey();
			}
		}
		return max;
	}

	/**
	 * Add keyword.
	 *
	 * @param statement the statement
	 * @throws IOException the io exception
	 */
	private void addKeyword(String statement) throws IOException {
		String newKeyword = detectKeyword(statement);
		System.out.printf("New Keyword: %s; What should the response be? (type no to cancel): ", newKeyword);
		String response = input.nextLine();

		if (response.trim().equalsIgnoreCase("no"))
			return;

		int id = getMaxKey(keywords) + 1;
		keywordsWriter.write(String.format("%s=%s",id, newKeyword));
		keywordsWriter.newLine();
		responsesWriter.write(String.format("%s=%s",id, response));
		responsesWriter.newLine();

		keywordsWriter.flush();
		responsesWriter.flush();

		Pair<Integer, String> keyword = new Pair<>(id, newKeyword);
		Pair<Integer, String> newResponse = new Pair<>(id, response);

		keywords.add(keyword);
		responses.add(newResponse);

		System.out.println();
	}

	/**
	 * Detect keyword string.
	 *
	 * @param statement the statement
	 * @return the string
	 */
	private String detectKeyword(String statement) {
		/*
		THIS DETECTS A RANDOM WORD: FOR NOW I AM JUST GOING TO USE WHOLE STATEMENT AS KEYWORD
		String[] words = statement.split(" ");
		int newKeywordPos = new Random().nextInt(words.length);
		return words[newKeywordPos];*/
		return statement;
	}

	/**
	 * Find keyword int.
	 *
	 * @param statement the statement
	 * @param goal      the goal
	 * @param startPos  the start pos
	 * @return the int
	 */
	private int findKeyword(String statement, String goal, int startPos) {
		String phrase = statement.trim().toLowerCase();
		goal = goal.toLowerCase();

		int pos = phrase.indexOf(goal, startPos);

		while (pos >= 0) {
			char before = ' ';
			char after = ' ';

			if (pos > 0) {
				before = phrase.charAt(pos - 1);
			}

			if (pos + goal.length() < phrase.length()) {
				after = phrase.charAt(pos + goal.length());
			}

			if (!(Character.isLetter(before) || Character.isLetter(after))) {
				return pos;
			}

			pos = phrase.indexOf(goal, pos + 1);
		}

		return -1;
	}
}
