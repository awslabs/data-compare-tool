const MIN_VERSION = 14;
const MAX_VERSION = 15;
const nodeVersion = process.version.replace(/^v/, '');
const [nodeMajorVersion] = nodeVersion.split('.');
console.log(`Current node version is ${nodeVersion}`)
if (nodeMajorVersion < MIN_VERSION || nodeMajorVersion > MAX_VERSION) {

    console.error(`node version ${nodeVersion} is incompatible with this module. ` +
        `Expected version is 14.19.1`);
    process.exit(1);
}