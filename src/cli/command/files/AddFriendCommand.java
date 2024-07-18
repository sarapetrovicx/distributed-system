package cli.command.files;

import app.AppConfig;
import cli.command.CLICommand;

public class AddFriendCommand implements CLICommand {
    @Override
    public String commandName() {
        return "add_friend";
    }

//    add_friend [adresa:ip] - Dodavanje čvora u listu prijatelja [localhost:9494]
//    ili mozemo da dajemo usernamove cvorovima, svejedno
//    (mogu biti i jednosmerne-odmah prijatelji i dvosmerne veze!!)


    //add_friend localhost:1300

    @Override
    public void execute(String args) {
        if(args != null){
            String address = args;

            boolean success = AppConfig.chordState.addFriend(address);

            if (success) {
                AppConfig.chordState.sendFriendRequest(address);
                AppConfig.timestampedStandardPrint("Successfully added friend: " + address);

            } else {
                AppConfig.timestampedErrorPrint("Failed to add friend: " + address);
            }
        } else {
            AppConfig.timestampedErrorPrint("Invalid arguments for add_friend. Usage: add_friend [address:ip]");
        }
    }
}
