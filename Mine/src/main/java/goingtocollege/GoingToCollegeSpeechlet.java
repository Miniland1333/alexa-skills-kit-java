/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package goingtocollege;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.*;
import goingtocollege.speechAssets.ListParings;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.testr.AdafruitREST;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class GoingToCollegeSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(GoingToCollegeSpeechlet.class);
    private static final HashMap<String,String> personParings = ListParings.personPairings;
    private static final HashMap<String,String> imagePairings = ListParings.imagePairings;



    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }


    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }


    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("GoingToCollege".equals(intentName)) {
            return getCollegeResponse(intent, session);
        } else if ("AdafruitIOset".equals(intentName)){
            return setAdafruit(intent, session);
        } else if ("AdafruitIOget".equals(intentName)) {
            return getAdafruit(intent, session);
        } else if ("setSwitch".equals(intentName)) {
            return setSwitch(intent, session);
        }else if ("AdafruitIOBoth".equals(intentName)){
            return AdafruitBoth(intent,session);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }


    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        //String speechText = "<speak> Welcome to Command <audio src = \"https://s3-us-west-1.amazonaws.com/testrforagnewskill/AliveInTheSummer.mp3\" /></speak>";
        String speechText = "Welcome to Command!";
        String cardText = "Welcome to Command!";
        String repromptText="";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Welcome");
        card.setContent(cardText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptText);
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getCollegeResponse(final Intent intent,final Session session) {
        Slot inputPerson = intent.getSlot("Person");
        String person = inputPerson.getValue();
        String repromptText = "";
        boolean isAskResponse = false;
        String speechText;
        String cardText;
        String imageURL;
        StandardCard card = new StandardCard();

        String answer = personParings.get(person.toLowerCase());
        WordUtils.capitalizeFully(person);

        switch (answer) {
            case "UNKNOWN":
                speechText = "I am not sure where " + person + " plans on going to college";
                cardText = speechText;
                break;
            case "CONFLICT":
                speechText = "Which did you mean: Cate Bode or Kathryn Anoskey?";
                cardText = "Which did you mean: Cate Bode, or Kathryn Anoskey";
                isAskResponse = true;
                repromptText = "Which did you mean: Cate Bode, or Kathryn Anoskey?";
                break;
            default:
                speechText = person + " plans on going to " + answer;
                cardText = speechText;
                imageURL = imagePairings.get(answer);
                Image logo = new Image();
                logo.setLargeImageUrl(imageURL);
                logo.setSmallImageUrl(imageURL);
                card.setImage(logo);
                break;
        }


        card.setTitle(person);
        card.setText(cardText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        //noinspection Duplicates
        if (isAskResponse)
        {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    private SpeechletResponse setSwitch(final Intent intent, final Session session){
        String state = intent.getSlot("State").getValue();
        state = state.toUpperCase();
        //log.info(state);
        String speechText;
        String cardText;
        try{
            AdafruitREST.post("state",state);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speechText="I turned the lights "+state;
            speech.setText(speechText);

            // Create the Simple card content.
            cardText = "I turned the lights "+state;
            SimpleCard card = new SimpleCard();
            card.setTitle("Status");
            card.setContent(cardText);

            return SpeechletResponse.newTellResponse(speech, card);

        }catch (Exception ignored){}
        return null;
    }
    private SpeechletResponse setAdafruit(final Intent intent,final Session session) {
        String inputColor=intent.getSlot("Color").getValue();
        String speechText;
        String cardText;

        Color color;
        try {
            Field field = Class.forName("java.awt.Color").getField(inputColor);
            color = (Color)field.get(null);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            String hex = String.format("#%02x%02x%02x", r, g, b);
            AdafruitREST.post("colors-of-the-wind",hex);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speechText="The lights are currently "+inputColor+".";
            speech.setText(speechText);

            // Create the Simple card content.
            cardText = "The lights are currently "+inputColor+".";
            SimpleCard card = new SimpleCard();
            card.setTitle("Status");
            card.setContent(cardText);

            return SpeechletResponse.newTellResponse(speech, card);
        } catch (Exception e) {
            color = null; // Not defined
        }
        return null;
    }
    private SpeechletResponse getAdafruit(final Intent intent,final Session session) {
        String color = AdafruitREST.get("colors-of-the-wind");
        String speechText;
        String cardText;

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speechText="The lights are currently "+color+".";
        speech.setText(speechText);

        // Create the Simple card content.
        cardText = "The lights are currently "+color+".";
        SimpleCard card = new SimpleCard();
        card.setTitle("Status");
        card.setContent(cardText);

        return SpeechletResponse.newTellResponse(speech, card);

    }
    private SpeechletResponse AdafruitBoth(final Intent intent, final Session session) {
        String inputColor = intent.getSlot("Color").getValue();
        String state = intent.getSlot("State").getValue();
        state = state.toUpperCase();
        String speechText;
        String cardText;

        Color color;
        try {
            Field field = Class.forName("java.awt.Color").getField(inputColor);
            color = (Color) field.get(null);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            String hex = String.format("#%02x%02x%02x", r, g, b);
            AdafruitREST.post("colors-of-the-wind", hex);
            AdafruitREST.post("state", state);


            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speechText="I turned the lights " + state + ", and they are currently " + inputColor + ".";
            speech.setText(speechText);

            // Create the Simple card content.
            cardText = "I turned the lights " + state + ", and they are currently " + inputColor + ".";
            SimpleCard card = new SimpleCard();
            card.setTitle("Status");
            card.setContent(cardText);

            return SpeechletResponse.newTellResponse(speech, card);
        } catch (Exception e) {
            color = null; // Not defined
        }
        return null;
    }


    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "Standing By";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("System Standing By");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
