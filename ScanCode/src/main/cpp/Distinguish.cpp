//
// Created by Zwj on 2022/6/16.
//

#include "Distinguish.h"
#include "XLog.h"

CodeBean Distinguish::zbarScan(Mat &qrcode_mat) {
    CodeBean codeBean;
    if (imageScanner == nullptr) {
        return codeBean;
    }
    int width = qrcode_mat.cols;
    int height = qrcode_mat.rows;
    auto *raw = (uchar *) qrcode_mat.data;
    Image imageZbar(width, height, "Y800", raw, width * height);
    imageScanner->scan(imageZbar); //扫描条码

    Image::SymbolIterator symbol = imageZbar.symbol_begin();

    for (; symbol != imageZbar.symbol_end(); ++symbol) {
        codeBean.code = symbol->get_data();
        codeBean.type = getCodeTypeString(symbol->get_type(), ZXing::BarcodeFormat::None);
    }
    imageZbar.set_data(nullptr, 0);
    return codeBean;
}

void Distinguish::getBarCode(Mat &src, vector<CodeBean> &codeBeans) {
    if (brcodeDetector == nullptr) {
        return;
    }
    Mat corners, result;
    if (brcodeDetector->detect(src, corners)) {
        for (int i = 0; i < corners.rows; ++i) {

            vector<Point2f> points(4);
            //topLeft
            points[0] = Point2f((float) corners.at<float>(i, 2),
                                (float) corners.at<float>(i, 3));

            //bottomLeft
            points[1] = Point2f((float) corners.at<float>(i, 0),
                                (float) corners.at<float>(i, 1));

            //bottomRight
            points[2] = Point2f((float) corners.at<float>(i, 6),
                                (float) corners.at<float>(i, 7));

            //topRight
            points[3] = Point2f((float) corners.at<float>(i, 4),
                                (float) corners.at<float>(i, 5));


            vector<Point2f> trans(4);
            trans[0] = Point2f(0, 0);
            trans[1] = Point2f(0, 200);
            trans[2] = Point2f(400, 200);
            trans[3] = Point2f(400, 0);
            Mat m = getPerspectiveTransform(points, trans);

            warpPerspective(src, result, m, cv::Size(400, 200), INTER_LINEAR);

            CodeBean codeBean = zbarScan(result);
            if (!codeBean.code.empty()) {
                RotatedRect rectPoint = minAreaRect(Mat(points));
                codeBean.center = rectPoint.center;
                codeBean.rect = rectPoint.boundingRect();
                codeBean.topLeft = points[0];
                codeBean.bottomLeft = points[1];
                codeBean.bottomRight = points[2];
                codeBean.topRight = points[3];
                codeBeans.push_back(codeBean);
            }
        }
    }
    result.release();
    corners.release();
}

void Distinguish::getQrCode(Mat &src, vector<CodeBean> &codeBeans) {
    if (detector == nullptr) {
        return;
    }
    vector<Mat> vPoints;
    vector<string> strDecoded;
    strDecoded = detector->detectAndDecode(src, vPoints);
    for (int i = 0; i < strDecoded.size(); ++i) {
        CodeBean codeBean;
        codeBean.topLeft = Point2f((float) vPoints[i].at<float>(0, 0),
                                   (float) vPoints[i].at<float>(0, 1));
        codeBean.bottomLeft = Point2f((float) vPoints[i].at<float>(3, 0),
                                      (float) vPoints[i].at<float>(3, 1));
        codeBean.bottomRight = Point2f((float) vPoints[i].at<float>(2, 0),
                                       (float) vPoints[i].at<float>(2, 1));
        codeBean.topRight = Point2f((float) vPoints[i].at<float>(1, 0),
                                    (float) vPoints[i].at<float>(1, 1));

        RotatedRect rectPoint = minAreaRect(Mat(vPoints[i]));
        codeBean.center = rectPoint.center;
        codeBean.rect = rectPoint.boundingRect();
        codeBean.type = "QRCODE";
        codeBean.code = strDecoded[i];
        codeBeans.push_back(codeBean);
    }
}

void Distinguish::release() {

    mux.lock();
    if (!qrCodes->empty()) {
        qrCodes->clear();
    }
    delete qrCodes;
    qrCodes = nullptr;

    delete imageScanner;
    imageScanner = nullptr;

    hints.release();
    hints = nullptr;

    detector.release();
    detector = nullptr;

    brcodeDetector.release();
    brcodeDetector = nullptr;

    delete javaCallHelper;
    javaCallHelper = nullptr;
    mux.unlock();
}


Distinguish::Distinguish(const string &detect_prototxt, const string &detect_caffe_model,
                         const string &sr_prototxt,
                         const string &sr_caffe_model) {


    qrCodes = new vector<CodeBean>();
    javaCallHelper = new JavaCallHelper();
    imageScanner = new ImageScanner();
    hints = makePtr<ZXing::DecodeHints>();
    hints->setTextMode(ZXing::TextMode::HRI);
    hints->setEanAddOnSymbol(ZXing::EanAddOnSymbol::Ignore);
    hints->setFormats(ZXing::BarcodeFormat::Any);

    imageScanner->set_config(ZBAR_NONE, ZBAR_CFG_ENABLE, 1);

    detector = makePtr<wechat_qrcode::WeChatQRCode>(detect_prototxt, detect_caffe_model,
                                                    sr_prototxt, sr_caffe_model);
    brcodeDetector = makePtr<BarcodeDetector>(sr_prototxt, sr_caffe_model);
}


void Distinguish::zxingScan(Mat &qrcode, vector<CodeBean> &codeBeans) {
    if(hints== nullptr){
        return;
    }
    cvtColor(qrcode, qrcode, COLOR_BGR2BGRA);

    auto *image = new ZXing::ImageView(qrcode.data, qrcode.cols, qrcode.rows,
                                       ZXing::ImageFormat::BGRX);
    auto results = ReadBarcodes(*image, *hints);
    for (auto &result: results) {
        if (result.isValid()) {
            CodeBean codeBean;
            codeBean.code = result.text();
            vector<Point2f> points(4);
            //topLeft
            points[0] = Point2f((float) result.position().topLeft().x,
                                (float) result.position().topLeft().y);

            //bottomLeft
            points[1] = Point2f((float) result.position().bottomLeft().x,
                                (float) result.position().bottomLeft().y);

            //bottomRight
            points[2] = Point2f((float) result.position().bottomRight().x,
                                (float) result.position().bottomRight().y);

            //topRight
            points[3] = Point2f((float) result.position().topRight().x,
                                (float) result.position().topRight().y);

            RotatedRect rectPoint = minAreaRect(Mat(points));
            codeBean.center = rectPoint.center;
            codeBean.rect = rectPoint.boundingRect();
            codeBean.topLeft = points[0];
            codeBean.bottomLeft = points[1];
            codeBean.bottomRight = points[2];
            codeBean.topRight = points[3];
            codeBean.type = getCodeTypeString(zbar::ZBAR_NONE, result.format());
            codeBeans.push_back(codeBean);
        } else if (result.error()) {
            XLOGE("%s", result.error().msg().c_str());
        }
    }
    delete image;

}

jobject Distinguish::decode(JNIEnv *env, ImageData *imageData) {
    mux.lock();
    if (qrCodes == nullptr) {
        mux.unlock();
        return nullptr;
    }

    qrCodes->clear();

    Mat src(imageData->height + imageData->height / 2,
            imageData->width, CV_8UC1,
            (uchar *) imageData->data);

    cvtColor(src, src, COLOR_YUV2GRAY_420);
    rotate(src, src, ROTATE_90_CLOCKWISE);

    if (imageData->boxWidth != 0 && imageData->boxTop != 0) {
        Rect rect = Rect(Point((src.cols - imageData->boxWidth) / 2,
                               imageData->boxTop),
                         Point((src.cols - imageData->boxWidth) / 2 +
                               imageData->boxWidth, imageData->boxTop + imageData->boxWidth));

        src = src(rect);
    }

    getQrCode(src, *qrCodes);

    getBarCode(src, *qrCodes);

    if (qrCodes->empty() && !simpleMode) {
        zxingScan(src, *qrCodes);
    }

    delete imageData;
    src.release();

    if (javaCallHelper != nullptr) {
        if (!qrCodes->empty()) {
            mux.unlock();
            return javaCallHelper->codeBeanToJava(env, *qrCodes);
        }
    }
    mux.unlock();
    return nullptr;
}

ZXing::ImageView Distinguish::ImageViewFromMat(const Mat &image) {
    using ZXing::ImageFormat;

    auto fmt = ImageFormat::None;
    switch (image.channels()) {
        case 1:
            fmt = ImageFormat::Lum;
            break;
        case 3:
            fmt = ImageFormat::BGR;
            break;
        case 4:
            fmt = ImageFormat::BGRX;
            break;
    }
    if (image.depth() != CV_8U || fmt == ImageFormat::None)
        return {nullptr, 0, 0, ImageFormat::None};

    return {image.data, image.cols, image.rows, fmt};
}

string
Distinguish::getCodeTypeString(zbar_symbol_type_e zbarType, ZXing::BarcodeFormat barcodeFormat) {
    if (zbarType != ZBAR_NONE) {
        switch (zbarType) {
            case ZBAR_PARTIAL:
                return "PARTIAL";
            case ZBAR_EAN2:
                return "EAN2";
            case ZBAR_EAN5:
                return "EAN5";
            case ZBAR_EAN8:
                return "EAN8";
            case ZBAR_UPCE:
                return "UPCE";
            case ZBAR_ISBN10:
                return "ISBN10";
            case ZBAR_UPCA:
                return "UPCA";
            case ZBAR_EAN13:
                return "EAN13";
            case ZBAR_ISBN13:
                return "ISBN13";
            case ZBAR_COMPOSITE:
                return "COMPOSITE";
            case ZBAR_I25:
                return "I25";
            case ZBAR_DATABAR:
                return "DataBar";
            case ZBAR_DATABAR_EXP:
                return "DataBarExpanded";
            case ZBAR_CODABAR:
                return "Codabar";
            case ZBAR_CODE39:
                return "Code39";
            case ZBAR_PDF417:
                return "PDF417";
            case ZBAR_QRCODE:
                return "QRCode";
            case ZBAR_CODE93:
                return "Code93";
            case ZBAR_CODE128:
                return "Code128";
            default:
                return "None";
        }

    } else if (barcodeFormat != ZXing::BarcodeFormat::None) {
        switch (barcodeFormat) {
            case ZXing::BarcodeFormat::Aztec:
                return "Aztec";
            case ZXing::BarcodeFormat::Codabar:
                return "Codabar";
            case ZXing::BarcodeFormat::Code39:
                return "Code39";
            case ZXing::BarcodeFormat::Code93:
                return "Code93";
            case ZXing::BarcodeFormat::Code128:
                return "Code128";
            case ZXing::BarcodeFormat::DataBar:
                return "DataBar";
            case ZXing::BarcodeFormat::DataBarExpanded:
                return "DataBarExpanded";
            case ZXing::BarcodeFormat::DataMatrix:
                return "DataMatrix";
            case ZXing::BarcodeFormat::EAN8:
                return "EAN8";
            case ZXing::BarcodeFormat::EAN13:
                return "EAN13";
            case ZXing::BarcodeFormat::ITF:
                return "ITF";
            case ZXing::BarcodeFormat::MaxiCode:
                return "MaxiCode";
            case ZXing::BarcodeFormat::PDF417:
                return "PDF417";
            case ZXing::BarcodeFormat::QRCode:
                return "QRCode";
            case ZXing::BarcodeFormat::UPCA:
                return "UPCA";
            case ZXing::BarcodeFormat::UPCE:
                return "UPCE";
            case ZXing::BarcodeFormat::MicroQRCode:
                return "MicroQRCode";
            default:
                return "None";
        }
    }
    return "None";
}























