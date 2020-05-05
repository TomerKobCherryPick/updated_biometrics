require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-biometrics-authenticator"
  s.version      = "1.0.0"
  s.summary      = "biometrics authenticator"
  s.license      = "MIT"

  s.author      = "tomer"
  s.homepage     = "https://google.com/"
  s.platform     = :ios, "9.0"

  s.source       = { :http => 'file' + __dir__ + '/'}
  s.source_files  = "ios/**/*.{h,m,swift}"

  s.dependency 'React'
end
