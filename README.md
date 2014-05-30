ayai-backend
============

This repository contains the Java and Scala source files for the backend components of the AyAI server.
These components are:

<ul>
<li>AI</li>
<li>Networking</li>
<li>Game World Management / Entity System</li>
</ul>

THIS PROJECT DOES NOT IMPORT WELL INTO ECLIPSE, USE A TOOL LIKE "SUBLIME TEXT" OR "VIM" TO EDIT.

These instructions are only to run the server.  To run the frontend code, look at the read me here:

https://github.com/ayaigame/ayai-frontend

Instructions for installing

Windows Installation
====================

First you need to install sbt, git and Java 7

http://www.scala-sbt.org/0.13.2/docs/Getting-Started/Setup.html

Download the SBT Windows MSI executable

You can download git gui and command line tools from 

http://git-scm.com/download/win

Download latest java version here

https://www.java.com/en/download/

RUNNING IN WINDOWS
==================

After these are installed you now have to clone the ayai-backend and ayai-frontend repositories

using git gui or git command line, in the directory you want, run :

"git clone https://github.com/ayaigame/ayai-backend.git"

to go to a new branch (it is not good to change code in master/develop) do:

"git branch new-branch"

then to run the server do

"sbt run" 


Mac
===


First you need to install sbt, git and Java 7

In the command line, If running Macports do :

"port install sbt"

If running homebrew do:

"brew install sbt"

Run the commands shown on the git page here to install git:

http://git-scm.com/book/en/Getting-Started-Installing-Git


Download latest java version here

https://www.java.com/en/download/

RUNNING IN WINDOWS
==================

After these are installed you now have to clone the ayai-backend and ayai-frontend repositories

using git gui or git command line, in the directory you want, run :

"git clone https://github.com/ayaigame/ayai-backend.git"

to go to a new branch (it is not good to change code in master/develop) do:

"git branch new-branch"

then to run the server do

"sbt run" 


RUNNING IN MAC
==============

Same instructions as windows


Why choose Scala
================

we had a diverse group of team members who excelled in different areas and we voted on a language that could use multiple languages on the JVM (Scala, Java, Clojure).  Scala is similar to java and can even use all of javas code (or you can even write in Java if you want).  But the main reason was for the use of Akka/Socko and its ease of doing concurrency.  Akka handles all our network traffic and we use it to be able to retrieve Items/Quests/Effects/Messages.  Instead of handling the blocking we are able to get all information with ease.

Akka uses things called Actors to handle these.  These are essentially threads that run on their own and do a task that is given to them (Look at ayai/gamestate/ItemMap or ayai/gamestate/MessageProcessor for more examples). These can be called from an actor system which is passed to most systems and holds all information about any actor.  These actors are initialized in the ayai/apps/GameLoop.scala file.

What is an Entity Component System or ECS
=========================================

Ayai is built around a system called an ECS.  An ECS has 4 main sections.  First is the World, the second is the Entity, and the third is the component, and the fourth is a system.

To get started a component is essentially a piece of data for a specific purpose (say health, attack, or velocity).  These components are then used in objects called an Entity (these could be characters, AI's, attacks, anything that has a unique set of data).  These entities are stored in something called a world (in AYAI worlds and rooms are the same thing).  A world has the ability to create, delete, and store entities and directs each entity to its correct system.  Which leads to the last section: Systems.  Systems should be the only thing manipulating data.  A system could be a movement system, a health system, an attack system, or a networking system.  They look for entities which contain a certain set of components and match against that, so there is no chance of that data missing. You can then manipulate and change the data in that system.

What is different about our project is that our MessageProcessor and MessageInterpreter have access to worlds and can create player entities/ItemUse/Attack/AI entities.  Which is why our ECS system has lists than can handle concurrency.

How to add AI
=============

So it would be a misnomer if you were not able to add AI in a project called AYAI!  And it essentially is one.  In Ayai the hooks are there to set up AI, but does need some more work to work more properly.  To set up AI at an entity level, you should create an AI Component which will either be just an identifier that the entity has AI or holds any information that AI specifically should know (maybe A* functions). Then you should create a system which then acts on that AI component look at src/ayai/systems for examples on how to create systems and to query for components.  You can thne create broad or narrow uses for AI based on what you need, you can have generic AI or specific AI.  So you could have AStar System that may work with only Astar components or have a generic AI Component that works in the same fashion for everything.

We do have some examples of entity AI in the DirectorSystem and GoalSystem, but these need major reworkings.  For something, on a global level, look at the mapgeneration folder inside of ayai/systems/ai/mapgeneration for how we connect and spawn Ai rooms.  This is  special case where this is used as an actor and is called from our networking system.


