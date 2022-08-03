
#ifndef SCAN_XTHREAD_H
#define SCAN_XTHREAD_H

void XSleep(int ms);

class XThread {
public:

    virtual void Stop();

    virtual void Main() {}

    virtual bool Start();

protected:
    bool isExit = true;
private:
    virtual void TextureMain();
};

#endif //SCAN_XTHREAD_H
