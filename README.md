# Pentagon Bullets


A multi-player battle arena game to play with your friends!


In Pentagon Bullets, you are a Pentagon in a 2D world, and you have the ability to shoot bullets. Your goal is simple: crush your opponents with your bullets and special abilities!

## Releases

A [Release](https://github.com/sherpal/PentagonBullets/releases) is available for Window and Linux

## The rules

Each player (hereafter, Pentagon) can move freely in the 2D plane at constant speed. By left-clicking (by default), you shoot a bullet towards your mouse. Unless special effect affects the game, a bullet goes twice as fast as a Pentagon. If about to die, each Pentagon can rely on a Shield that lasts 5s and protects from all the damages. The cooldown (aka the time before the shield gets available again) is 60s.


The whole point of the game is to shoot at your opponents while avoiding their bullets.


Each player starts with 100 health, and a bullet deals 10 damages. The reach of a bullet is the distance it would travel in 5s.

### Special abilities

Aside from the Shield, each Pentagon can choose one of the following special abilities to either crush their opponents faster, or to get extra protection. Each ability has a "cooldown", which is the time before it is available again.

- Big Bullet: shoots a bullet that is 3 times bigger, 2 times faster and deals 3 times as much as a usual bullet. Cooldown: 10s.
- Penta Shot: shoots 5 bullets ranging from angle -pi/16 to pi/16 towards the mouse position. These 5 bullets go 5/4 times faster than the usual bullets. Cooldown: 10s.
- Teleportation: the player is instantly transported to the mouse position. Cooldown: 20s.
- Healing Zone: places at the mouse position a circular zone that heals any team member that stands in it by 5 health every 0.5s. The zone stays for 60s, or when having healed for 40 health, whichever comes first. Cooldown: 30s.
- Bullet Amplifier: places at the mouse position a rectangular area for 10s. Each time a bullet from your team passes through it, its size, as well as the damage it deals, is doubled. Cooldown: 15s.
- Smash Bullet: shoots a huge bullet that grows over time. When this bullet hits an opponent, their loses half of their remaining life, rounded down (it is therefore impossible to die from the Smash Bullet). The reach of the smash bullet is twice as smaller than the one of a usual bullet, but it can go through walls. Cooldown: 15s.
- Gun Turret: places at the Pentagon position a turret that shoots at the closest opponent (within 7/10th of a bullet reach) every 0.2s. A turret has 100 health, and is destroyed when its health goes to 0. It otherwise stays forever. Cooldown: 30s.
- Barrier: places at the mouse position a square region that lasts for 5s. This region acts for your opponents as a wall, stopping all bullets and keeping your opponents from passing through it. Cooldown: 20s.
- Bullet Glue: slows down all enemy bullets by dividing their current speed by 2. Additionally, all bullets shot by your opponents during the 5 next seconds have also their speed divided by 2. Does not affect the Smash Bullet. Cooldown: 20s.
- Laser: this ability happens in two steps. The first step, the player places at the mouse position a Laser Launcher that will stay until the next activation of the ability. When the ability is cast again, it fires a laser between the Laser Launcher and the player, dealing 30 damages to every enemy (Pentagons and Turrets alike) between the two. The laser has a triangle form. The three vertices of the triangle are the center of the caster, and two vertices of a segment passing through the center of the Laser Launcher, and perpendicular to the line caster-launcher. The length of that segment is two times the length of a Pentagon. There must be at least 1s between the two steps. Cooldown: 8s. (/!\ This ability is still under testing and will be balanced in the future.)

The abilities have long been tested and should be fairly balanced. Feel free to give feedback if you think that an ability is too weak or strong. 

### Other game features

The game takes place in a square in which Pentagons are free to move, except for a few walls. Boundaries and walls are white. Bullets and Pentagon can't pass through walls, with the only exception of the Smash Bullet. The size of the game depends on the number of players.


During the game, every 5 seconds, a Heal Unit pops at a random position and lasts for 15s. If a Pentagon takes it, they are healed for 15 health.


Every 7s, a Damage Zone pops at a random position, with a starting radius of 5. Every 2s, its radius grows by 2, until it reaches 150, at which point it disappears. All Pentagons standing in a Damage Zone are dealt 5 damage every 0.5s, cumulative.


### Game Modes

Two different game modes are currently available.

- The standard mode. This a free for all "king of the hill" type of game, although playing in teams is also possible. The last man (or team) standing wins the game. In this mode, when a player dies, its ability falls on the ground and any player can take it. If a player has n times an ability, then the cooldown is divided by n. The size of the game area depends on the number of players. If there are more than two players, then during the game, a Mist grows every 2s from the boundaries in order to restrain the game area. Every Pentagon standing in the Mist loses 5 health every 0.5s. The Mist stops growing when the remaining free area is the size of a two players game.
- Capture the Flag mode. Two teams fight in order to get the flag of opponent's team back to your side. The first team that brings the flag three times wins. A team can't bring the flag back if the other team has their flag. In this mode, when a Pentagon dies, it reappears 25s later at the starting position. In each side, two defending turrets stand. If a turret is destroyed, it reappears 25s later.

## How to play

One of the players has to create a server from the app, by going into "Create Server" and entering the port the server will be listening to. Then that player can host a game by going into "Host Game", chose a name for theirselfs, a name for the game (that basically behaves like a password, although no encryption is used), the address of the computer of the server (if the player created the server on their computer, it is localhost) and the port.
To join a game, the same information must be provided.

Note that the server can be on a computer different from the one of each player.

Remark: if you want to play with people that are not on the same wifi network as you are, you will probably have to do some extra work. Indeed, you have to set up a special NAT rule on your router, to redirect incoming packets coming through the game port. You should look at your Internet Access Provider for help doing so.


All players and the server should be on the same continent in order to have a satisfying gaming experience.

## Technology

The game clients use [Electron](https://electronjs.org/) with [Scala.js](https://www.scala-js.org/). The game menus are made in HTML and CSS, while the game itself is entirely painted on a canvas using [pixi.js](http://www.pixijs.com/).


The game logic is implemented in Scala and is purely functional. A Game State is updated via Game Actions that occur during the game. These actions are communicated between the clients and the server using UDP message and [boopickle](https://github.com/suzaku-io/boopickle) for serializing case classes. The server currently also is a node.js application (made with Scala.js), but the plan for the future is to make a server as a stand alone Scala application.


When fastOptimizing, CTRL+D opens the dev tools.


## Issues

The game has some known bugs that we are constantly trying to fix. Feel free to report any bugs or weird behaviour that happens.
