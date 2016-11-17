package se.coada.id2212.hw1.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;


public class Hangman {
	
	private final String WORDFILE = "words.txt";
	private final int GUESSES = 6;
	private final int WONGAME = -1;
	
	private int remainingGuesses;
	private int scorePoints;
	private char[] obfuscatedWord;
	private String cleanWord;
	
	public Hangman()
	{
		scorePoints = 0;
		remainingGuesses = 0;
	}
	
	/*
	 * Handles all of the file openings and readings
	 */
	private String getWord()
	{
		String word = "Programming";
		try {
			// Open words.txt
			BufferedReader br = new BufferedReader(new FileReader(WORDFILE));
			String tempWord;
			
			for (int i = 0; i < 10; i++)									// Read random number of lines
			{
				if ( (tempWord = br.readLine()) == null) break;				// EOF
				word = tempWord;											// Assign new word to cleanWord
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			System.err.println("Hangman failed opening " + WORDFILE + ", will set word to 'programming'");
		}
		catch (IOException e) {
			System.err.println("Hangman opened " + WORDFILE + " but something happened");
		}
		
		return word;
	}
	
	public GameState newGame()
	{
		remainingGuesses = GUESSES;						// reset the number of guesses
		
		cleanWord = getWord();							// generate a new word
						
		cleanWord = cleanWord.toUpperCase();			// make sure word is uppercase
		
		// obfuscate word returned to player
		obfuscatedWord = new char[cleanWord.length()];
		Arrays.fill(obfuscatedWord, '-');
		
		return state();
	}
	
	public GameState guess(char c)
	{
		if (remainingGuesses > 0)
		{
			int firstIndex;
			c = Character.toUpperCase(c);
						
			if ( (firstIndex = cleanWord.indexOf(c)) != -1)					// if word contains char
			{
				for (int i = firstIndex; i < cleanWord.length(); i++)
				{
					if (cleanWord.charAt(i) == c)							// this char in word is char
					{
						if (obfuscatedWord[i] == c)							// this char in obf. has already been revealed
						{
							badGuess();										// consider this char guess as bad
							return state();
						}
						obfuscatedWord[i] = c;								// reveal the char
					}
				}
				if (new String(obfuscatedWord).equals(cleanWord)) victory();
			}
			else badGuess();
		}
		return state();
	}
	
	public GameState guess(String w)
	{
		if (remainingGuesses > 0)
		{
			w = w.toUpperCase();
			if (w.equals(cleanWord)) victory();
			else badGuess();
		}
		return state();
	}
	
	private void badGuess()
	{
		remainingGuesses--;
		if (remainingGuesses == 0) obfuscatedWord = ("Game Over! Correct word was " + cleanWord).toCharArray();
	}
	
	private void victory()
	{
		scorePoints++;
		obfuscatedWord = ("Congratulations! You guessed " + cleanWord).toCharArray();
		remainingGuesses = WONGAME;
	}
	
	private GameState state()
	{
		return new GameState(remainingGuesses, scorePoints, obfuscatedWord);
	}
	
	// The game state object, to be returned to the player
	public static class GameState implements Serializable
	{
		public final int guesses;
		public final String word;
		public final int score;
		
		public GameState(int remainingGuesses, int scorePoints, char[] obfuscatedWord)
		{
			guesses = remainingGuesses;
			score = scorePoints;
			
			if (obfuscatedWord == null) word = "";
			else word = new String(obfuscatedWord);
		}
	}
	
}
