default_platform(:android)

platform :android do
  desc "Deploy an test version to the Google Play"
  lane :test do
    gradle(task: "clean bundleRelease")
    upload_to_play_store(track: 'test')
  end
end