package cli.command.files;

import app.AppConfig;
import cli.command.CLICommand;
import mutex.DistributedMutex;
import mutex.TokenMutex;
import servent.message.files.AskViewFilesMessage;
import servent.message.util.MessageUtil;

public class ViewFilesCommand implements CLICommand {

    private TokenMutex mutex;

    public ViewFilesCommand(DistributedMutex mutex) {
        this.mutex = (TokenMutex) mutex;
    }

    @Override
    public String commandName() {
        return "view_files";
    }

    //    view_files [adresa:ip] - Pregled javnih i privatnih datoteka od prijatelja
    //    ili nekog drugog čvora. - ispis u konzoli koje fajlove mogu da vidim (prijatelj i
    //    javne i privatne, neprijatelj samo javne)
    @Override
    public void execute(String args) {
        String[] splitArgs = args.split(" ");

        if (splitArgs.length == 1) {
            String address = splitArgs[0];

            AskViewFilesMessage askViewFilesMessage = new AskViewFilesMessage(AppConfig.myServentInfo.getListenerPort(),
                    Integer.parseInt(address.split(":")[1]));
            MessageUtil.sendMessage(askViewFilesMessage);

        } else {
            AppConfig.timestampedErrorPrint("Invalid arguments for view_files. Usage: view_files [address:ip]");
        }
    }
}
