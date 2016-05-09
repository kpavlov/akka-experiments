package hello;

import akka.actor.UntypedActor;

import java.util.function.Consumer;

class EventListenerActor extends UntypedActor {

    private final Consumer<AbstractEvent> eventListener;

    public EventListenerActor(Consumer<AbstractEvent> eventListener) {
        this.eventListener = eventListener;
    }

    public void onReceive(Object message) {
        if (message instanceof AbstractEvent) {
            eventListener.accept((AbstractEvent) message);
        }
    }
}
