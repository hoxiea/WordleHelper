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

Lots of choices! But let's not get ahead of ourselves. At this point, we just need to make our opening guess.

Before we ask WordleHelper's opinion, let's think for a second about what a good opening guess (or any guess, really) should accomplish.

Without any feedback from Wordle, all we know is that the secret word is a relatively well-known 5-letter English word. Imagine a huge pool of thousands of words, all of them possibilities at this point. As you know if you've played Wordle before, the feedback we receive about the individual letters will help us narrow down this pool of possibilities until we've guessed the secret word correctly.

If it's the individual letters that we receive feedback on, then we should make sure to guess a word consisting of letters that will be informative. But what makes a letter informative?

To motivate the idea of an informative letter, consider first two examples of uninformative letters

```
> b
SOARE: 115.8
AROSE: 115.8
AEROS: 115.8
AESIR: 114.7
SERAI: 114.7
RAISE: 114.7
REAIS: 114.7
ARISE: 114.7
ALOES: 113.0
LASER: 113.0
```

So sure enough, your friend's suggestion about SOARE being the best word seems legitimate from an information theory perspective. Let's try it!

```
> g
What did you guess?
> SOARE
And what did Wordle tell you about SOARE? (1: miss, 2: cold, 3: hot)
> 11131
Registered SOARE
46 possible words remain!

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

Wow, that one guess and its feedback tells us that only 46 words are still possible (no S, O, A, or E, and an R in the fourth position). What should we guess next?

```
> b
HYLIC: 108.4
CULTI: 107.1
LICHT: 106.1
ITCHY: 104.2
TICHY: 104.2
HILUM: 102.6
LYTIC: 101.6
CULTY: 101.6
FICHU: 100.7
HUMIC: 100.7
```

Those wouldn't have been my first thought, either.

##### Q: Why HYLIC?

WordleHelper is trying to guess as many letters as possible that will help us narrow down the space of remaining possibilities. H, Y, L, I, and C probably aren't the absolute five most informative letters, but they're the five most informative letters that can be combined into a valid guess, so that's WordleHelper's top suggestion. In other words, getting Wordle's feedback about these five letters is most likely to narrow the space of possibilities as much as possible.

##### Q: But you're ignoring the R we know is in the fourth position!

It's true, we are. And yes, I know: by doing so, we're making it impossible to guess the correct word in only 2 guesses.

But here's the thing: if we guess a new word with an R in the fourth position, we'll only learn new information about four letters, not five, since we already know about that R in the fourth position. This isn't optimal! Getting feedback about five new letters is better than four. And in particular, we should be trying to get feedback about the five letters that are most likely to eliminate possibilities.

##### I really want to get it in 2 though! I'll bet I could get it!

OK, let's follow that train of thought.

If you'd really like to try to guess the word in 2 guesses, you can ask for a list of all possible words:

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
> a
BLURB
BLURT
BURRY
CHIRK
CHIRL
CHIRM
CHIRP
CHIRR
CHIRT
CHIRU
CHURL
CHURN
CHURR
CIRRI
CURRY
DURRY
FIRRY
FLIRT
FLURR
FURRY
GRRRL
GURRY
HURRY
INDRI
INURN
KIRRI
KNURL
KNURR
KUKRI
LIBRI
LURRY
MURRI
MURRY
MYRRH
NITRY
QUIRK
QUIRT
THIRD
THIRL
THURL
TWIRL
TWIRP
UHURU
UPDRY
WHIRL
WHIRR
```

Many of these words in WordleHelper's dictionary are unlikely to be the answer, based on their obscurity - they make great guesses to give us more information about letters, but they're too rare to likely be the secret word. But how are you going to choose between BLURB, BLURT, CHIRP, CHURN, CURRY, FLIRT, FURRY, HURRY, QUIRK, THIRD, TWIRL, and WHIRL (probably common enough to be the secret word)?

You could just guess one and see what you learn. But you have a <10% chance of being correct, and if you're wrong and you selected a word with letters that don't overlap much with other words, then you haven't actually learned much.

OK, so we're committed to guessing again.
