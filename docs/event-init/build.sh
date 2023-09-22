#/bin/bash
goreleaser --snapshot --skip-publish --rm-dist
cp dist/event-init_darwin_amd64_v1/event-init ./event-init-mac
cp dist/event-init_linux_amd64_v1/event-init ./event-init-linux
cp dist/event-init_windows_amd64_v1/event-init.exe ./event-init.exe