
public class Hangman {
	
	private final int GUESSES = 6;
	private final int WONGAME = -1;
	
	private int remainingGuesses;
	private int score;
	private String obfuscatedWord;
	private String cleanWord;
	
	public Hangman()
	{
		score = 0;
                System.out.println("Hey");
	}
	
	public GameState newGame()
	{
		remainingGuesses = GUESSES;
		
		// TODO: Open words.txt
		// TODO: Read random number of lines
		// TODO: Assign new word to cleanWord
		
		// We only work with lower case
		cleanWord = cleanWord.toLowerCase();
		
		// obfuscate word returned to player
		obfuscatedWord = StringUtils.repeat("-", cleanWord.length);
		
		return new GameState();
	}
	
	public GameState guess(char c)
	{
		if (remainingGuesses > 0)
		{
			int firstIndex;
			
			c = Character.toLowerCase(c);
			
			// if word contains c and it has never been guessed before
			if ( (firstIndex = cleanWord.indexOf(c)) != -1 && obfuscatedWord.indexOf(c) == -1)
			{
				// TODO: start at firstIndex and reveal all c in obfuscatedWord
				
				if (obfuscatedWord.equals(cleanWord)) victory();
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
		if (remainingGuesses == 0) obfuscatedWord = "Game Over! Correct word was " + cleanWord;
	}
	
	private void victory()
	{
		score++;
		obfuscatedWord = "Congratulations! You guessed " + cleanWord;
		remaningGuesses = WONGAME;
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
			this.obfuscatedWord = Hangman.this.obfuscatedWord;
			this.score = Hangman.this.score;
		}
	}
	
}
