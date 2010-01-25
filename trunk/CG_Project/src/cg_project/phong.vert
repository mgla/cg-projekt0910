varying vec3 normal;
varying vec3 cameraDirection;
varying vec3 lightDirection[3];

void main()
{
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    normal = normalize(gl_NormalMatrix * gl_Normal);

    //Calculate the direction to the camera and to
    //the light for that vertex
    vec3 vertex = (gl_ModelViewMatrix * gl_Vertex).xyz;
    cameraDirection = -vertex;

    /************************************************************************
     * Original solution. 
     * Notice: here the built-in variable gl_LightSource is used in the for loop.
     * We found that on NVIDIA GeForce9400/9600 Graphic Card of MacBook Pro, if you use 
     * gl_LightSource[i] in a for loop, the FPS will be very slow. It is PERHAPS a bug of 
     * Apple or NVIDIA. 
     *************************************************************************/
    /*
    for(int i = 0; i < 3; ++i)
    {
        lightDirection[i] = gl_LightSource[i].position.xyz - vertex;
    }
    */

    /************************************************************************
     * Walkaround solution below. 
     * If the situation we mentioned above happens, replace it with the code below.
     *************************************************************************/
    
    gl_LightSourceParameters lightSource[3];
    lightSource[0] = gl_LightSource[0];
    lightSource[1] = gl_LightSource[1];
    lightSource[2] = gl_LightSource[2];

    for(int i = 0; i < 3; ++i)
    {
        lightDirection[i] = lightSource[i].position.xyz - vertex;
    }
}
