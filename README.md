# DiscordDND
 My Discord DND personal assistant.
```
!help : This message
!image : A helpful image for people new to DND.
!set S W : Set attribute S to W
!add S W : Add W to attribute S
!sub S W : Subtract W from attribute S
!get S : Show my attribute S
!roll [a][d(b)][kc]\[+/-e] : Show result of rolling
  ...'b' sided dice 'a' times (default 1)
  ...keeping 'c' of them (default all, negative number for keeping worst dice)
  ...and adding 'e' (default 0).
  [parts in braces are optional]
  example: !r 5d20k-2+5 rolls 5 d20s, sums the worst two, and adds 5.
!st S : Roll a Saving Throw for a given attribute S
!save : Save your attributes for next time.

S is a string (text), W is a whole number. Stuff [in brackets] is optional.

Many attributes have abbreviations - for example, i or int will both equal intelligence.
```
