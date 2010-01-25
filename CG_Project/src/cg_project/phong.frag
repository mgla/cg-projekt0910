varying vec3 normal;
varying vec3 cameraDirection;
varying vec3 lightDirection[3];

uniform float lightSwitchedOn[3];

void main (void)
{
    //gl_FragColor = vec4(0.0,0.0,1.0,1.0);
    vec4 finalColor = gl_FrontMaterial.emission + gl_FrontMaterial.ambient * gl_LightModel.ambient;
    vec3 normalizedNormal = normalize(normal);

    /************************************************************************
     * Original solution. 
     * Notice: here the built-in variable gl_LightSource is used in the for loop.
     * We found that on NVIDIA GeForce9400/9600 Graphic Card of MacBook Pro, if you use 
     * gl_LightSource[i] in a for loop, the FPS will be very slow. It is PERHAPS a bug 
     * of Apple or NVIDIA. 
     *************************************************************************/
    /*
    for(int i = 0; i < 3; ++i)
    {
        vec3 normalizedLightDirection = normalize(lightDirection[i]);
        float lambertTerm = lightSwitchedOn[i] * max(dot(normalizedNormal, normalizedLightDirection), 0.0);
        finalColor += lambertTerm * gl_LightSource[i].diffuse * gl_FrontMaterial.diffuse;
        vec3 normalizedCameraDirection = normalize(cameraDirection);
        vec3 reflectionDirection = reflect(-normalizedLightDirection, normalizedNormal);
        float specular = pow(max(dot(reflectionDirection, normalizedCameraDirection), 0.0), gl_FrontMaterial.shininess);
        finalColor += lightSwitchedOn[i] * specular * gl_LightSource[i].specular * gl_FrontMaterial.specular;
    }
    gl_FragColor = finalColor;
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
        vec3 normalizedLightDirection = normalize(lightDirection[i]);
        float lambertTerm = lightSwitchedOn[i] * max(dot(normalizedNormal, normalizedLightDirection), 0.0);
        finalColor += lambertTerm * lightSource[i].diffuse * gl_FrontMaterial.diffuse;
        vec3 normalizedCameraDirection = normalize(cameraDirection);
        vec3 reflectionDirection = reflect(-normalizedLightDirection, normalizedNormal);
        float specular = pow(max(dot(reflectionDirection, normalizedCameraDirection), 0.0), gl_FrontMaterial.shininess);
        finalColor += lightSwitchedOn[i] * specular * lightSource[i].specular * gl_FrontMaterial.specular;
    }
    gl_FragColor = finalColor;
}
