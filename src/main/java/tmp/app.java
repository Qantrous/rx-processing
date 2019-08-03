package tmp;

import tmp.controller.Controller;
import tmp.controller.StartEvent;
import tmp.controller.StartProcessEvent;
import tmp.controller.StopEvent;
import tmp.player.Player;
import tmp.processor.AudioProcessor;

import java.util.ArrayList;
import java.util.Scanner;

public class app {


    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        Controller controller = new Controller();
        ArrayList<Player> players = new ArrayList<>();
        ArrayList<AudioProcessor> audioProcessors = new ArrayList<>();

        for (int i = 0; i < 2; i ++) {
            Player p = new Player(i, 0);
            players.add(p);
            controller.subscribeToPlayer(p);


            AudioProcessor audioProcessor = new AudioProcessor(i);
            audioProcessors.add(audioProcessor);
            controller.subscribeToProcessor(audioProcessor);
        }

        audioProcessors.forEach(Thread::start);
//        players.forEach(Thread::start);

        int id = 0;

        for (; ; ) {
            scanner.next();
            if (!players.get(0).isRunning()) {
                controller.getInstance().post(new StartEvent(id++));
            } else {
                controller.getInstance().post(new StopEvent());
            }
        }

//        controller.getInstance().post(new StartEvent(0));

    }
}
