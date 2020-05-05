import React, { Component } from 'react'
import {View, Text, TouchableOpacity, StyleSheet , NativeModules} from 'react-native'
import BiometricsAuthenticator from 'react-native-biometrics-authenticator'

class App extends Component {
  constructor(props) {
    super(props)
    this.state = {
      text: 'Authenticate',
      buttonColor: 'grey'
    }
  }

  onSuccess = () => {
    debugger
    this.setState({text: 'Success :)', buttonColor: 'green'})
  }

  onFailure = (error) => {
    debugger
    if (error === BiometricsAuthenticator.errors.noAuthenticationOnDevice) {
      this.setState({text: 'No Authentication avialable', buttonColor: 'yellow'})
    } else if (error === BiometricsAuthenticator.errors.authenticationFailed) {
      this.setState({text: 'Fail :/ , try again', buttonColor: 'red'})
    }
  }

  aut


  render () {
    return (
      <View style={styles.container}>
        <TouchableOpacity
          style={[styles.touchableOpacity, {backgroundColor: this.state.buttonColor}]}
          onPress={async () => {
            debugger
            await BiometricsAuthenticator.authenticate(this.onSuccess, this.onFailure)}
          }
          >
          <Text style={styles.text}>
            {this.state.text}
          </Text>
        </TouchableOpacity>
      </View>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  touchableOpacity: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 120,
    height: 50,
    borderRadius: 5,
  },
  text: {
    fontSize: 14
  }
})

export default App
