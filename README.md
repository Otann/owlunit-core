# What is it?

This is core part of sevice I was implementing back then

It's main purpose is to store so called `Ii`s (Information Items), their connection and compare them in terms of their "likeness". Intention was to build recommendation system where you can compare anything to anything.

And basing on that feature recommendation engine could be implemented, where profile is described as `Ii` with connections to preferences and current mood as `Ii`s that describes movie genre or food preferences. Then traversal performed and each found item compared to profile and query.

# Practical points

Basicly I used most of this code for [Talk to IO project](http://talkto.io) I've been working on in 2014.
You can try to following steps below to provide artifact for another code in neighbour repository [owlunit-web](http://github.com/otann/owlunit-web)

# How to use

1. Clone repository with `git clone git@github.com:OwlUnit/core.git`
1. [Install SBT](https://github.com/harrah/xsbt/wiki/Getting-Started-Setup)
1. Run in the bash/console `$ sbt`
1. Initialize IntelliJ IDEA project with `> gen-idea` or `> gen-idea no-classifiers` to make things little happen little bit faster
1. Compile in SBT with `> compile`
1. Run tests in SBT with `> test`
1. Publish artifact to local Ivy2 repository with `sbt publish-local`
