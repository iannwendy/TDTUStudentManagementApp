#!/usr/bin/env python3
"""
Script để tạo ERD (Entity Relationship Diagram) từ file Mermaid (.mmd)
Sử dụng @mermaid-js/mermaid-cli để render diagram

Cài đặt dependencies:
    npm install -g @mermaid-js/mermaid-cli

Hoặc sử dụng online API (không cần cài đặt):
    Script sẽ tự động sử dụng mermaid.ink API nếu mmdc không có sẵn
"""

import subprocess
import sys
import os
import urllib.request
import urllib.parse

def check_mmdc_installed():
    """Kiểm tra xem mermaid-cli đã được cài đặt chưa"""
    try:
        result = subprocess.run(['mmdc', '--version'], 
                              capture_output=True, 
                              text=True, 
                              timeout=5)
        return result.returncode == 0
    except (subprocess.TimeoutExpired, FileNotFoundError):
        return False

def generate_with_mmdc(input_file, output_file):
    """Generate ERD sử dụng mermaid-cli (mmdc)"""
    try:
        cmd = ['mmdc', '-i', input_file, '-o', output_file]
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
        
        if result.returncode == 0:
            print(f"✅ ERD diagram đã được tạo thành công bằng mmdc!")
            print(f"📄 File output: {output_file}")
            return True
        else:
            print(f"❌ Lỗi khi chạy mmdc:")
            print(result.stderr)
            return False
    except subprocess.TimeoutExpired:
        print("❌ Lỗi: Timeout khi chạy mmdc")
        return False
    except Exception as e:
        print(f"❌ Lỗi: {e}")
        return False

def generate_with_api(input_file, output_file):
    """Generate ERD sử dụng mermaid.ink API (online)"""
    try:
        # Đọc nội dung file .mmd
        with open(input_file, 'r', encoding='utf-8') as f:
            mermaid_content = f.read()
        
        # Encode content để gửi lên API
        encoded = urllib.parse.quote(mermaid_content)
        
        # API endpoint
        api_url = f"https://mermaid.ink/img/{encoded}"
        
        print("🔄 Đang tải diagram từ mermaid.ink API...")
        
        # Download image
        urllib.request.urlretrieve(api_url, output_file)
        
        print(f"✅ ERD diagram đã được tạo thành công từ API!")
        print(f"📄 File output: {output_file}")
        return True
        
    except Exception as e:
        print(f"❌ Lỗi khi sử dụng API: {e}")
        return False

def main():
    input_file = 'erd.mmd'
    output_png = 'erd.png'
    output_svg = 'erd.svg'
    
    print("=" * 60)
    print("TDTU Student Information Management System")
    print("ERD Diagram Generator (Mermaid)")
    print("=" * 60)
    print()
    
    # Kiểm tra file input
    if not os.path.exists(input_file):
        print(f"❌ Không tìm thấy file: {input_file}")
        print("   Vui lòng tạo file erd.mmd trước!")
        sys.exit(1)
    
    # Kiểm tra xem có mmdc không
    has_mmdc = check_mmdc_installed()
    
    if has_mmdc:
        print("✅ Tìm thấy mermaid-cli (mmdc)")
        print("🔄 Đang tạo ERD diagram (PNG)...")
        success_png = generate_with_mmdc(input_file, output_png)
        
        print()
        print("🔄 Đang tạo ERD diagram (SVG)...")
        success_svg = generate_with_mmdc(input_file, output_svg)
        
        if success_png and success_svg:
            print()
            print("=" * 60)
            print("✅ Hoàn thành! Các file diagram đã được tạo:")
            print(f"   - {output_png} (raster image)")
            print(f"   - {output_svg} (vector image)")
            print("=" * 60)
        else:
            print()
            print("⚠️  Có lỗi xảy ra khi tạo diagram")
            sys.exit(1)
    else:
        print("⚠️  Không tìm thấy mermaid-cli (mmdc)")
        print("   Đang sử dụng mermaid.ink API (online)...")
        print()
        print("💡 Để cài đặt mermaid-cli (khuyến nghị):")
        print("   npm install -g @mermaid-js/mermaid-cli")
        print()
        
        print("🔄 Đang tạo ERD diagram (PNG) từ API...")
        success = generate_with_api(input_file, output_png)
        
        if success:
            print()
            print("=" * 60)
            print(f"✅ Hoàn thành! File diagram đã được tạo: {output_png}")
            print("=" * 60)
        else:
            print()
            print("❌ Không thể tạo diagram. Vui lòng:")
            print("   1. Cài đặt mermaid-cli: npm install -g @mermaid-js/mermaid-cli")
            print("   2. Hoặc kiểm tra kết nối internet để sử dụng API")
            sys.exit(1)

if __name__ == '__main__':
    main()
