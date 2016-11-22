# Tic-tac-toe

The classic tic-tac-toe game for Android devices. 

Protocol Logic: 

This app only allows two players to play tic-tac-toe

1.Asks the user to enter a name

2.a.joins a random group

displays the first available group that the user can join

if there are no available groups that the user can join
returns to the group options

b.creates a group
creates a group and asks the user to wait for an opponent

c.joins a group that I know
joins a group that the user types

Who ever joins the group last makes the first move
The two players take turns playing the game and communicate with each other by sending messages
format of messages: MSG, @group_name, row_number column_number
for example: MSG,@nihao,00

When it is the opponent's turn to make a move and the opponent has not made a move,
the board is disabled and the user cannot make a move.
In addition, the players cannot click a board button that has been already been clicked.

The game ends when there’s a winner or when the the board is full and there’s a tie

