package com.amin;

import java.io.*;
import java.util.*;

public class Magpie2 {
	private Map<Integer, String> keywords;
	private Map<Integer, String> responses;
	private Scanner input;
	private BufferedWriter keywordsWriter;
	private BufferedWriter responsesWriter;

	public Magpie2(Scanner input) {
		this.input = input;
		init();
	}

	private void init() {
		this.keywords = new HashMap<>();
		this.responses = new HashMap<>();

		File keywordsTxt = new File("src/com/amin/keywords.txt");
		File responsesTxt = new File("src/com/amin/responses.txt");

		try {
			BufferedReader reader = new BufferedReader(new FileReader(keywordsTxt));
			String line = reader.readLine();
			while (line != null) {
				String[] arr = line.split("=");
				int associated = Integer.parseInt(arr[0]);
				String rest = arr[1];
				keywords.put(associated, rest);
				line = reader.readLine();
			}

			reader = new BufferedReader(new FileReader(responsesTxt));
			line = reader.readLine();
			while (line != null) {
				String[] arr = line.split("=");
				int associated = Integer.parseInt(arr[0]);
				String rest = arr[1];
				responses.put(associated, rest);
				line = reader.readLine();
			}

			this.keywordsWriter = new BufferedWriter(new FileWriter(keywordsTxt, true));
			this.responsesWriter = new BufferedWriter(new FileWriter(responsesTxt, true));
		} catch (IOException ignored) {}
	}

	public String getGreeting() {
		return "Hello, let's talk.";
	}

	public String getResponse(String statement) {
		String response = "continue...";

		statement = statement.trim();

		if (statement.length() == 0)
			response = "Please say something";

		boolean foundKeyword = false;

		for (Map.Entry<Integer, String> entry : keywords.entrySet()) {
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
						String tempResponse = responses.get(entry.getKey());
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
					String tempResponse = responses.get(entry.getKey());

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

	private void addKeyword(String statement) throws IOException {
		String newKeyword = detectKeyword(statement);
		System.out.printf("New Keyword: %s; What should the response be? (type no to cancel): ", newKeyword);
		String response = input.nextLine();

		if (response.trim().equalsIgnoreCase("no"))
			return;

		int id = Collections.max(keywords.keySet()) + 1;
		keywordsWriter.write(String.format("%s=%s",id, newKeyword));
		keywordsWriter.newLine();
		responsesWriter.write(String.format("%s=%s",id, response));
		responsesWriter.newLine();

		keywordsWriter.flush();
		responsesWriter.flush();

		keywords.put(id, newKeyword);
		responses.put(id, response);

		System.out.println();
	}

	private String detectKeyword(String statement) {
		/*
		THIS DETECTS A RANDOM WORD: FOR NOW I AM JUST GOING TO USE WHOLE STATEMENT AS KEYWORD
		String[] words = statement.split(" ");
		int newKeywordPos = new Random().nextInt(words.length);
		return words[newKeywordPos];*/
		return statement;
	}

	/**
	 * Search for one word in phrase. The search is not case
	 * sensitive. This method will check that the given goal
	 * is not a substring of a longer string (so, for
	 * example, "I know" does not contain "no").
	 *
	 * @param statement the string to search
	 * @param goal      the string to search for
	 * @param startPos  the character of the string to begin the
	 *                  search at
	 * @return the index of the first occurrence of goal in
	 * statement or -1 if it's not found
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
