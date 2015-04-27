CBPatcher
==========
Allows plugins (or any java app) to easily modify classes at runtime

##Features
- Allows for custom events
- Make other plugins compatible with your plugin
- **ONLY FOR ADVANCED USERS**

##Usage
1. Define a class you want to overide
````java
@Inject(injectInto=WorldServer.class)
public EvilWorldServer {
    public void ag() {
        throw new RuntimeException("I'm evil");
    }
}
````
2. Pass it into `BukkitCbPatcher` preferiably before CBPatcher is loaded
````java
BukkitCBPatcher.inject(EvilWorldServer.class);
````

##FAQ
- Does this have a performance cost?
  - Only at startup

- Why put it in a plugin instead of letting plugins roll their own
  - To make plugins more compatible with each other, and increase performance, an make it generally easier for everyone
