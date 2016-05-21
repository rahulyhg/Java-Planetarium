package com.haurentziu.render;

import com.haurentziu.starchart.Observer;
import com.jogamp.opengl.GL3;

/**
 * Created by haurentziu on 20.05.2016.
 */

public class Renderer {
    protected Shader shader;

    protected Renderer(String vertShader, String geomShader, String fragShader){
        shader = new Shader();
        shader.loadAllShaders(vertShader, geomShader, fragShader);
    }

    public void initialize(GL3 gl){
        shader.init(gl);
    }

    public void setSize(GL3 gl, float width, float height){
        shader.useShader(gl);
        shader.setVariable(gl, "width", width);
        shader.setVariable(gl, "height", height);
    }

    public Shader getShader(){
        return shader;
    }

    public void setZoom(GL3 gl, float zoom){
        shader.useShader(gl);
        shader.setVariable(gl, "zoom", zoom);
    }

    public void setSideralTime(GL3 gl, float sideralTime){
        shader.useShader(gl);
        shader.setVariable(gl, "sideral_time", sideralTime);
    }

    public void setRotation(GL3 gl, float azRot, float altRot){
        shader.useShader(gl);
        shader.setVariable(gl, "azimuth_rotation", azRot);
        shader.setVariable(gl, "altitude_rotation", altRot);
    }

    public void setLocation(GL3 gl, float longitude, float latitude){
        shader.setVariable(gl, "observer_latitude", latitude);
        shader.setVariable(gl, "observer_longitude", longitude);
    }

    public void delete(GL3 gl){
        shader.deleteProgram(gl);
    }

    protected void setObserver(GL3 gl, Observer observer){
        shader.setVariable(gl, "zoom", (float)observer.getZoom());
        shader.setVariable(gl, "azimuth_rotation", (float)observer.getAzRotation());
        shader.setVariable(gl, "altitude_rotation", (float)observer.getAltRotation());
        shader.setVariable(gl, "sideral_time", (float)observer.getSideralTime());
        shader.setVariable(gl, "observer_latitude", (float)observer.getLatitude());
        shader.setVariable(gl, "observer_longitude", (float)(observer.getLongitude()));
    }

}
