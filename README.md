## Java RestAPI
An advanced, yet super lightweight java rest api, created by yours truly.\
Meant for creating backend webservers and for use in other backend projects.

#### How to use
```java
// Main Class
public class TestRestServer {
    public static void main(String[] args) {
        // You may set the port to whatever port you wish to use.
        WebAPI webAPI = new WebAPI(port);
        
        // Now you can add handlers, handlers are classes which contain routes.
        // Since this handler's main route is test, it will be localhost:port/test/
        webAPI.registerHandler("/test", new TestHandler());
    }
}

// Handler Class
public class TestHandler {
    // You may now add however many routes you desire inside of this class.
    // Keep in mind that every route made inside this class, will come after
    // the main route, making it localhost:port/test/route
    
    // Get requests require you to set the return type to a bson document.
    // You can download this dependency from the official mongodb repository.
    @Route(name = "/", type = Route.Type.GET)
    public Document mainHandler(List<String> unknownParameters) {
        // This handler would be the root of the /test/ route,
        // making it's actual link localhost:port/test/
        
        // This will return JSON with a param 'Code' with the value '200'
        return new Document("Code", "200");
    }
    
    // Post requests require you to have the return type set to void.
    @Route(name = "/simplepost", type = Route.Type.POST)
    public Document mainHandler(List<String> unknownParameters) {
        // This handler's actual link would be localhost:port/test/simplepost
        
        System.out.println("Somebody used our post request!");
    }

    // You can also have "fill in the blank" requests which look like this.
    // You define one by putting arrow brackets around it.
    @Route(name = "/profiles/<name>", type = Route.Type.GET)
    public Document mainHandler(List<String> unknownParameters) {
        // This handler's actual link would be localhost:port/profiles/<name>
        // You can fill in name with whatever you would like.
        // These are called Unknown Parameters and are accessed via the list.
        // To access the name, you would do the following because it's the first.
        
        // If you used the route localhost:port/test/profiles/Gleeming then
        // the following code would return a bson object with the value "Gleeming"
        return new Document("PROFILE", unknownParameters.get(0));
    }

    // Here's an example of two Unknown Parameters with a known parameter after it.
    @Route(name = "/profiles/<firstname>/<lastname>/getaddress", type = Route.Type.GET)
    public Document mainHandler(List<String> unknownParameters) {
        String firstName = unknownParameters.get(0);
        String lastName = unknownParameters.get(1);

        return new Document("FIRST-NAME", firstName).append("LAST-NAME", lastName).append("ADDRESS", methodToGetAddress(firstName, lastName));
    }
}
```
