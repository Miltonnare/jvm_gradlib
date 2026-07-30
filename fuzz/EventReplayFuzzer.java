import com.aura.engine.StateMachine;
import com.aura.engine.EventReplayEngine;

public class EventReplayFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        try {
            // Setup a state machine with a few typical mock states
            StateMachine fsm = new StateMachine("INIT");
            fsm.addTransition("INIT", new StateMachine.Transition("start", "RUNNING", null, null));
            fsm.addTransition("RUNNING", new StateMachine.Transition("pause", "PAUSED", "pause_allowed", "true"));
            fsm.addTransition("PAUSED", new StateMachine.Transition("resume", "RUNNING", null, null));
            fsm.addTransition("RUNNING", new StateMachine.Transition("stop", "STOPPED", null, null));
            fsm.addTransition("STOPPED", new StateMachine.Transition("reset", "INIT", null, null));

            EventReplayEngine engine = new EventReplayEngine(fsm);
            engine.replay(data);
        } catch (Exception e) {
            // Ignore expected syntax, state transition, and buffer overflow exceptions
        }
    }
}
