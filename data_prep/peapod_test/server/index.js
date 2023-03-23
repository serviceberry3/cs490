// dependencies
const http = require("http");

// PORT
const PORT = 3000;

// server create
const server = http.createServer((req, res) => {
   if (req.url === "/") {
      res.write("This is home page.");
      res.end();
   } else if (req.url === "/about" && req.method === "GET") {
      res.write("This is about page.");
      res.end();
   } else {
      res.write("Not Found!");
      res.end();
   }
});

// server listen port
server.listen(PORT);

console.log(`Server is running on PORT: ${PORT}`);