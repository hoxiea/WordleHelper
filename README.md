# WordleHelper

This command line utility will help you improve your [Wordle](https://www.nytimes.com/games/wordle/index.html) performance by:

- Suggesting great words to guess, based on the feedback you've received
- Letting you provide a word you guessed and its feedback, so that future guesses can be better
- Listing all valid words, based on the feedback you've received

## Usage

After installing [Leinengin](https://leiningen.org/), you should just be able to start WordleHelper using:

```
lein run
```

## Example & Discussion

As an example of how you might use WordleHelper, consider the following interaction (where the input you provide comes after a `>`):

```
> lein run

Let's play Wordle!
What would you like to do?
q: Quit Wordle Helper
w: Score a word you're considering guessing
s: Print current status
l: Most common letters in remaining words
g: Enter your next guess
c: Toggle high-contrast mode (current: true)
b: Best words to guess
a: List all possible words
u: Undo your most recent guess
```

I like to use SOARE as my opening word, so I'll go ahead and guess SOARE in Wordle (Jan 30, 2023):

![](/README-imgs/soare.png)

Let's tell WordleHelper about our first guess:

```
> g
What did you guess? SOARE
And what did Wordle tell you about SOARE? (1: miss, 2: cold, 3: hot) 11323
Registered SOARE
SOARE
39 possible words remain!

What would you like to do?
q: Quit Wordle Helper
w: Score a word you're considering guessing
s: Print current status
l: Most common letters in remaining words
g: Enter your next guess
c: Toggle high-contrast mode (current: true)
b: Best words to guess
a: List all possible words
u: Undo your most recent guess
```

What a great guess! Only 39 words in WordleHelper's dictionary are compatible with that feedback. We can see them all using `a`:

```
> a
ARAME
BRACE
BRAKE
BRAME
BRANE
BRAVE
BRAZE
CRAKE
CRAME
CRANE
CRAPE
CRATE
CRAVE
CRAZE
DRAKE
DRAPE
DRAVE
FRAME
FRAPE
FRATE
GRACE
GRADE
GRAME
GRAPE
GRATE
GRAVE
GRAZE
IRADE
IRATE
PRATE
REAME
REATE
REAVE
TRACE
TRADE
TRAPE
TRAVE
URATE
WRATE
```

Many of these words might make great guesses but are unlikely to be the actual secret word because they tend to choose common words. Still, there are more than a few common words in there: BRACE, BRAKE, BRAVE, CRANE, CRATE, CRAVE, DRAPE, FRAME, GRACE, etc.

We should probably make another guess to narrow down the space of possibilities even further. Which word would do the best job of providing useful information?

```
What would you like to do?
q: Quit Wordle Helper
w: Score a word you're considering guessing
s: Print current status
l: Most common letters in remaining words
g: Enter your next guess
c: Toggle high-contrast mode (current: true)
b: Best words to guess
a: List all possible words
u: Undo your most recent guess

> b
COMPT: 66.4
CUBIT: 60.7
DEMPT: 60.4
GITCH: 60.0
DIACT: 58.3
DICTS: 58.3
CLIPT: 58.3
EVICT: 58.3
TICED: 58.3
MICHT: 58.3
```

I wouldn't have considered COMPT, but it's actually an incredible guess if our goal is to distinguish between the common words I listed above. Let's see what kind of feedback we get from Wordle:

![](/README-imgs/compt.png)

Let's provide WordleHelper with this feedback:

```
What would you like to do?
q: Quit Wordle Helper
w: Score a word you're considering guessing
s: Print current status
l: Most common letters in remaining words
g: Enter your next guess
c: Toggle high-contrast mode (current: true)
b: Best words to guess
a: List all possible words
u: Undo your most recent guess

> g
What did you guess? compt
And what did Wordle tell you about COMPT? (1: miss, 2: cold, 3: hot) 31111
Registered COMPT
SOARE
COMPT
4 possible words remain!

CRAKE
CRANE
CRAVE
CRAZE
```

Hmm... CRAKE is pretty unlikely, but the other three words are all common enough to be possibilities. If we started guessing them, we'll win in 3, 4, or 5 guess, depending on how lucky we are with our guesses. Or we could guess a single word that will tell us about N/V/Z, the only letter that differs between our three possibilities. Can you think of a word that would tell us enough to narrow it down?

```
What would you like to do?
q: Quit Wordle Helper
w: Score a word you're considering guessing
s: Print current status
l: Most common letters in remaining words
g: Enter your next guess
c: Toggle high-contrast mode (current: true)
b: Best words to guess
a: List all possible words
u: Undo your most recent guess

> b
KNAVE: 56.3
ZONKS: 56.3
KRANZ: 56.3
KNIVE: 56.3
ZINKY: 56.3
KANZU: 56.3
ZINKE: 56.3
LIKIN: 37.5
VISNE: 37.5
RINKS: 37.5
```

KNAVE should work! Let's go ahead and guess it in Wordle:

![](/README-imgs/knave.png)

So sure enough, CRAVE is what we're looking for:

![](/README-imgs/crave.png)

Splendid!
