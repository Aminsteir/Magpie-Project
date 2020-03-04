package com.amin;

import java.util.Scanner;

/**
 * A simple class to run the Magpie class.
 * @author Laurie White
 * @version April 2012
 */
public class MagpieRunner2
{

	/**
	 * Create a Magpie, give it user input, and print its replies.
	 */
	public static void main(String[] args)
	{
		Scanner in = new Scanner (System.in);
		Magpie2 maggie = new Magpie2(in);

		System.out.println (maggie.getGreeting());
		String statement = in.nextLine();
		
		while (!statement.toLowerCase().equals("bye"))
		{
			System.out.println (maggie.getResponse(statement));
			statement = in.nextLine();
		}
	}

}
