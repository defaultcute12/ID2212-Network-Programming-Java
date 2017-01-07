# Server

- JavaEE is used for the server, which uses websocket for communication.
- The server runs on Glassfish 4.1.1.
- The server (and web admin) runs at http://localhost:8080/NET-PRO-Server/
- *WebsocketServer* listens at http://localhost:8080/NET-PRO-Server/play/
- Messages are formated with JSON.

- *UserHandler* handles login and stores logged in users.
- User data (id, name, password, score) is stored inside *User*.
- Upon login the user data is fetched by *UserHandler* using JPA.
- JPA connects to MySQL as database.
- *UserHandler* maps logged in users to their websocket session.

- *LobbyHandler* handles creation, removal and listing of lobbies.
- *Lobby* handles joining, leaving, starting game and notifying players of changes.
- Future implementation for *Lobby* could be a chat feature.
- *Lobby* interacts with the *Game* object every time a player performs a move. If the method is successful it returns a message (game state) which the *Lobby* then sends to its players.

- The *Game* interface specifies requirements to what a game must implement.
- The only implemented game currently is *TicTacToe*.