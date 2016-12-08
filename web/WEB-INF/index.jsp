<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <meta name="description" content="">
    <meta name="author" content="">

    <title>Welcome to iBei</title>

    <!-- Bootstrap core CSS -->
    <link href="bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="bootstrap/styles/signin.css" rel="stylesheet">
</head>
    <body>
        <div class="container">
            <form class="form-signin" method="post">
                <h2 class="form-signin-heading">Please log-in</h2>
                <label for="inputUsername" class="sr-only">USERNAME</label>
                <input name="username" type="email" id="inputUsername" class="form-control" placeholder="USERNAME" required autofocus>
                <label for="inputPassword" class="sr-only">PASSWORD</label>
                <input name="password" type="password" id="inputPassword" class="form-control" placeholder="PASSWORD" required>
                <button class="btn btn-lg btn-primary btn-block" type="submit">Log-in</button>
            </form>
        </div> <!-- /container -->
    </body>
</html>
