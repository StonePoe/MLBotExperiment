package gCore;

import gCore.entity.DefaultDialoguePolicy;
import gCore.entity.DefaultDialogueStatus;
import gCore.proto.DialoguePolicy;
import gCore.proto.DialogueStatus;
import gCore.proto.Ontology;

import java.util.Map;

public class DialogueMonitor {
    private Map<String, String> cfg;
    private Ontology ontology;
    private DialogueStatus dialogueStatus;
    private DialoguePolicy dialoguePolicy;

    public DialogueMonitor(Map cfg) {
        this.cfg = cfg;
    }

    public boolean initialize() {
        dialogueStatus = new DefaultDialogueStatus();
        dialoguePolicy = new DefaultDialoguePolicy();
        return true;
    }

    public boolean reset() {
        dialogueStatus = new DefaultDialogueStatus();
        dialoguePolicy = new DefaultDialoguePolicy();
        return true;
    }

    public void inputDialogue() {

    }

    public void outputDialogue() {

    }

    public void endDialogue() {

    }

    public void log() {

    }
}
