package se.coada.id2212.hw1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


public class Hangman {
	
	private final String WORDFILE = "words.txt";
	private final int GUESSES = 6;
	private final int WONGAME = -1;
	
	private int remainingGuesses;
	private int score;
	private char[] obfuscatedWord;
	private String cleanWord;
	
	public Hangman()
	{
		score = 0;
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
				word = tempWord;										// Assign new word to cleanWord
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
						
		cleanWord = cleanWord.toLowerCase();			// make sure word is lowercase
		
		// obfuscate word returned to player
		obfuscatedWord = new char[cleanWord.length()];
		Arrays.fill(obfuscatedWord, '-');
		
		return new GameState();
	}
	
	public GameState guess(char c)
	{
		if (remainingGuesses > 0)
		{
			int firstIndex;
			c = Character.toLowerCase(c);
						
			if ( (firstIndex = cleanWord.indexOf(c)) != -1)					// if word contains char
			{
				for (int i = firstIndex; i < cleanWord.length(); i++)
				{
					if (cleanWord.charAt(i) == c)							// this char in word is char
					{
						if (obfuscatedWord[i] == c)							// this char in obf. has already been revealed
						{
							badGuess();										// consider this char guess as bad
							return new GameState();
						}
						obfuscatedWord[i] = c;								// reveal the char
					}
				}
				if (new String(obfuscatedWord).equals(cleanWord)) victory();
			}
			else badGuess();
		}
		return new GameState();
	}
	
	public GameState guess(String w)
	{
		if (remainingGuesses > 0)
		{
			w = w.toLowerCase();
			if (w.equals(cleanWord)) victory();
			else badGuess();
		}
		return new GameState();
	}
	
	private void badGuess()
	{
		remainingGuesses--;
		if (remainingGuesses == 0) obfuscatedWord = ("Game Over! Correct word was " + cleanWord).toCharArray();
	}
	
	private void victory()
	{
		score++;
		obfuscatedWord = ("Congratulations! You guessed " + cleanWord).toCharArray();
		remainingGuesses = WONGAME;
	}
	
	// The game state object, to be returned to the player
	public class GameState
	{
		public final int remainingGuesses;
		public final String obfuscatedWord;
		public final int score;
		
		public GameState()
		{
			this.remainingGuesses = Hangman.this.remainingGuesses;
			this.obfuscatedWord = new String(Hangman.this.obfuscatedWord);
			this.score = Hangman.this.score;
		}
	}
	
}
