default_platform(:android)

platform :android do
  desc "Deploy an internal version to the Google Play"
  lane :internal do
    gradle(task: "clean bundleRelease")
    upload_to_play_store(track: 'internal')
  end
end