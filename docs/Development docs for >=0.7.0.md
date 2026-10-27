## Stopping animations on the last frame [Issue #60 - Original repo](https://github.com/FoundationGames/Animatica/issues/60)
By default animations have loop set to true, devs can now add `loop = false` to an animation's properties file to only the animation once and stop it on last frame.
By default, animatica's own config uses `when_drawn` mode where things like closing and reopening a menu, etc. will redraw the animation.
As of `0.7.0+beta.4` pack creators can't change it by themselves, users have to do it rn. There exists another mode called `on_reload` where the animation does not redraw until the pack is reloaded or the game is restarted.
Yes, by the time 0.7.0 is out of beta pack creators "should" be able to set this using the properties file too.