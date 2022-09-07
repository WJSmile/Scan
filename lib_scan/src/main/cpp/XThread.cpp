
#include "XThread.h"
#include <thread>

using namespace std;

void XSleep(int ms) {
    chrono::milliseconds du(ms);
    this_thread::sleep_for(du);
}

bool XThread::Start() {
    isExit = false;
    thread th(&XThread::TextureMain, this);
    th.detach();
    return true;
}

void XThread::Stop() {
    isExit = true;
}

void XThread::TextureMain() {
    Main();
}